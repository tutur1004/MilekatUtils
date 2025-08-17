package fr.milekat.utils.messaging.adapter.rabbitmq;

import com.rabbitmq.client.*;
import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import fr.milekat.utils.messaging.MessagingChanel;
import fr.milekat.utils.messaging.MessagingConnection;
import fr.milekat.utils.messaging.MessagingVendor;
import fr.milekat.utils.messaging.ReceivedMessage;
import fr.milekat.utils.messaging.exceptions.MessagingLoadException;
import fr.milekat.utils.messaging.exceptions.MessagingSendException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class RabbitMQConnection implements MessagingConnection {
    private final MileLogger logger;
    private final ConnectionFactory connectionFactory;
    private final MessagingChanel rabbitMQConfig;
    private final String messageTag = "JSON_MESSAGE"; // Custom tag to filter messages

    // Track active consumers for cleanup
    private final ConcurrentMap<String, Channel> activeConsumers = new ConcurrentHashMap<>();

    // Track consumer configurations for re-registration after reconnection
    private final ConcurrentMap<String, Map.Entry<String, Consumer<ReceivedMessage>>> registeredProcessors = new ConcurrentHashMap<>();

    private volatile Connection connection;
    private String exchangeName;

    public RabbitMQConnection(@NotNull Configs config, @NotNull MileLogger logger) {
        this.logger = logger;

        // Fetch connections vars from config.yml file
        String host = config.getString("messaging.rabbitmq.hostname");
        int port = config.getInt("messaging.rabbitmq.port", 5672);
        String username = config.getString("messaging.rabbitmq.username", "null");
        String password = config.getString("messaging.rabbitmq.password", "null");

        // Debug hostname/port
        logger.debug("Hostname: " + host);
        logger.debug("Port: " + port);
        logger.debug("Username: " + username);
        logger.debug("Password: " + new String(new char[password.length()]).replace("\0", "*"));

        // Get RabbitMQ configuration from config.yml
        this.rabbitMQConfig = new MessagingChanel(
                config.getString("messaging.rabbitmq.exchange", "milekat_exchange"),
                config.getString("messaging.rabbitmq.type", "x-rtopic")
        );

        // Init the connection factory
        this.connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        // Enable automatic recovery for underlying connection stability
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setNetworkRecoveryInterval(5000);
        connectionFactory.setRequestedHeartbeat(30);

        // Initialize connection
        try {
            initConnection();
            connectionReady();
        } catch (MessagingLoadException e) {
            throw new MessagingLoadException("Couldn't connect to RabbitMQ server");
        }
    }

    @Override
    public synchronized void initConnection() throws MessagingLoadException {
        // Close existing connection if any
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
            } catch (Exception ignored) {
            }
        }

        // Clear active consumers (they will be recreated)
        activeConsumers.clear();

        try {
            connection = connectionFactory.newConnection();
            logger.info("RabbitMQ connection established");
        } catch (IOException | TimeoutException e) {
            throw new MessagingLoadException("Error while trying to init RabbitMQ connection: " + e.getMessage());
        }

        try (Channel ignored = connection.createChannel()) {
            logger.debug("RabbitMQ channel created successfully");
        } catch (Exception e) {
            throw new MessagingLoadException("Error while trying to init RabbitMQ channel: " + e.getMessage());
        }

        // Re-register all consumers after reconnection
        reRegisterAllConsumers();
    }

    /**
     * Re-register all previously registered consumers after a reconnection
     */
    private void reRegisterAllConsumers() {
        if (registeredProcessors.isEmpty()) {
            return;
        }

        logger.info("Re-registering " + registeredProcessors.size() + " consumers after reconnection");

        registeredProcessors.forEach((processorName, messageHandler) -> {
            try {
                createConsumer(processorName, messageHandler.getKey(), messageHandler.getValue());
                logger.info("Successfully re-registered consumer for queue: " + processorName);
            } catch (Exception e) {
                logger.warning("Failed to re-register consumer for queue '" + processorName + "': " + e.getMessage());
            }
        });
    }

    @Override
    public boolean connectionReady() {
        if (connection == null || !connection.isOpen()) {
            return false;
        }

        // Try to create a channel to verify connection health
        try (Channel ignored = connection.createChannel()) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() {
        logger.info("Closing RabbitMQ connection");

        // Cancel all active consumers
        activeConsumers.clear();
        registeredProcessors.clear();

        if (connection != null) {
            try {
                connection.close();
                logger.info("RabbitMQ connection closed successfully");
            } catch (IOException e) {
                logger.warning("Error while closing RabbitMQ connection: " + e.getMessage());
            }
        } else {
            logger.warning("RabbitMQ connection is already closed or was never initialized");
        }
    }

    @Override
    public MessagingVendor getVendor() {
        return MessagingVendor.RABBITMQ;
    }

    /**
     * Send a message with JSON format including tag and callback routing key
     *
     * @param targetRoutingKey The routing key to send the message to
     * @param senderCallBackKey The routing key for callback/reply messages
     * @param message The actual message content
     * @throws MessagingSendException if sending fails
     */
    public void sendMessage(String targetRoutingKey, String senderCallBackKey, String message)
            throws MessagingSendException {
        if (!connectionReady()) {
            initConnection();
        }

        try (Channel channel = connection.createChannel()) {
            // Create JSON message format
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("TAG", messageTag);
            jsonMessage.put("senderCallBackKey", senderCallBackKey);
            jsonMessage.put("message", message);

            String jsonString = jsonMessage.toString();

            logger.debug("Sending JSON message to routing key '" + targetRoutingKey + "': " + jsonString);

            channel.basicPublish(exchangeName, targetRoutingKey, null, jsonString.getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            throw new MessagingSendException("Error while creating JSON message: " + e.getMessage());
        } catch (Exception e) {
            throw new MessagingSendException("Error while sending message to RabbitMQ: " + e.getMessage());
        }
    }

    /**
     * Register a message processor for a specific queue
     *
     * @param processorName  The queue to consume from
     * @param messageHandler Function that receives ReceivedMessage (routing key + message + ack capability)
     */
    @Override
    public void registerMessageProcessor(@NotNull String processorName, @NotNull String routingKey,
                                         @NotNull Consumer<ReceivedMessage> messageHandler)
            throws MessagingLoadException {
        if (registeredProcessors.containsKey(processorName))
            throw new MessagingLoadException("Processor with name '" + processorName + "' is already registered");
        try {
            if (!connectionReady()) {
                initConnection();
            }

            // Store the processor configuration for re-registration after reconnects
            registeredProcessors.put(processorName, new AbstractMap.SimpleEntry<>(routingKey, messageHandler));

            // Create the actual consumer
            createConsumer(processorName, routingKey, messageHandler);

            logger.info("Registered message processor for queue: " + processorName);

        } catch (Exception e) {
            throw new MessagingLoadException("Error while registering message processor: " + e.getMessage());
        }
    }

    /**
     * Create the actual RabbitMQ consumer using modern DeliverCallback approach
     */
    private void createConsumer(String processorName, String routingKey, Consumer<ReceivedMessage> messageHandler)
            throws IOException {
        // Create a dedicated channel for this consumer
        Channel consumerChannel = connection.createChannel();

        // Ensure the queue exists
        consumerChannel.exchangeDeclare(rabbitMQConfig.getName(), rabbitMQConfig.getType());
        consumerChannel.queueDeclare(processorName, false, true, true, null);
        consumerChannel.queueBind(processorName, rabbitMQConfig.getName(), routingKey);

        // Create the delivery callback
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String rawMessage = new String(delivery.getBody(), StandardCharsets.UTF_8);

                logger.debug("Received raw message from queue '" + processorName + "': " + rawMessage);

                // Try to parse as JSON
                JSONObject jsonMessage;
                try {
                    jsonMessage = new JSONObject(rawMessage);
                } catch (JSONException e) {
                    logger.debug("Message is not valid JSON, ignoring: " + rawMessage);
                    // Acknowledge the message to remove it from queue
                    consumerChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    return;
                }

                // Check if message has our custom tag
                if (!jsonMessage.has("TAG") || !messageTag.equals(jsonMessage.getString("TAG"))) {
                    logger.debug("Message does not have the required TAG '" + messageTag + "', ignoring");
                    // Acknowledge the message to remove it from queue
                    consumerChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    return;
                }

                // Extract callback routing key and actual message
                String senderCallBackKey = jsonMessage.optString("senderCallBackKey", null);
                String actualMessage = jsonMessage.optString("message", "");

                logger.debug("Processed JSON message from queue '" + processorName +
                        "' with callback key '" + senderCallBackKey + "'");

                // Create ReceivedMessage wrapper using the callback routing key
                ReceivedMessage rabbitMessage = new RabbitMessage(consumerChannel,
                        delivery.getEnvelope().getDeliveryTag(), routingKey, senderCallBackKey, actualMessage);

                // Call the user-provided message handler
                messageHandler.accept(rabbitMessage);

                // Auto-reject if user forgot to ack/reject
                if (!rabbitMessage.isAcknowledged()) {
                    logger.warning("Message was not acknowledged or rejected by handler, auto-rejecting");
                    rabbitMessage.reject();
                }

            } catch (Exception e) {
                logger.warning("Error processing message: " + e.getMessage());
                // Reject the message and don't requeue to avoid infinite loops
                try {
                    consumerChannel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                } catch (IOException ioException) {
                    logger.warning("Failed to nack message: " + ioException.getMessage());
                }
            }
        };

        // Cancel callback (optional, for logging)
        CancelCallback cancelCallback = consumerTag -> logger.info("Consumer cancelled: " + consumerTag);

        // Start consuming
        String consumerTag = consumerChannel.basicConsume(processorName, false, deliverCallback, cancelCallback);

        // Track the consumer for cleanup
        activeConsumers.put(consumerTag, consumerChannel);
    }

    /**
     * Unregister a message processor (stops consuming and removes from auto-recovery)
     *
     * @param processorName The processor name to unregister
     */
    @Override
    public void unregisterMessageProcessor(String processorName) throws MessagingSendException {
        registeredProcessors.remove(processorName);

        // Find and stop the corresponding consumer
        activeConsumers.entrySet().removeIf(entry -> {
            try {
                Channel channel = entry.getValue();
                if (channel.isOpen()) {
                    channel.basicCancel(entry.getKey());
                    channel.close();
                }
                return true;
            } catch (Exception e) {
                logger.warning("Error stopping consumer: " + e.getMessage());
                return true;
            }
        });

        logger.info("Unregistered message processor for queue: " + processorName);
    }
}