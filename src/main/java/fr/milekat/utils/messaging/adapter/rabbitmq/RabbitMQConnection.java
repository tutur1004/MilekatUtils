package fr.milekat.utils.messaging.adapter.rabbitmq;

import com.rabbitmq.client.*;
import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import fr.milekat.utils.messaging.MessagingConnection;
import fr.milekat.utils.messaging.MessagingVendor;
import fr.milekat.utils.messaging.ReceivedMessage;
import fr.milekat.utils.messaging.adapter.rabbitmq.RabbitMQConfigProvider;
import fr.milekat.utils.messaging.adapter.rabbitmq.RabbitMessage;
import fr.milekat.utils.messaging.exceptions.MessagingLoadException;
import fr.milekat.utils.messaging.exceptions.MessagingSendException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class RabbitMQConnection implements MessagingConnection {
    private final MileLogger logger;
    private final ConnectionFactory connectionFactory;
    private final RabbitMQConfigProvider rabbitMQConfig;

    // Track active consumers for cleanup
    private final ConcurrentMap<String, Channel> activeConsumers = new ConcurrentHashMap<>();

    // Track consumer configurations for re-registration after reconnection
    private final ConcurrentMap<String, Consumer<ReceivedMessage>> registeredProcessors = new ConcurrentHashMap<>();

    private volatile Connection connection;
    private String exchangeName;

    public RabbitMQConnection(@NotNull Configs config, @NotNull MileLogger logger) {
        this.logger = logger;
        this.rabbitMQConfig = new RabbitMQConfigProvider(config);

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

        // Init the connection factory
        this.connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        // Optional: Enable automatic recovery for underlying connection stability
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
            throw new MessagingLoadException("Error while trying to init RabbitMQ connection");
        }

        try (Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(rabbitMQConfig.getRabbitExchange(), rabbitMQConfig.getRabbitExchangeType());
            this.exchangeName = rabbitMQConfig.getRabbitExchange();
            channel.queueDeclare(rabbitMQConfig.getRabbitQueue(), true, false, false, null);
            channel.queueBind(rabbitMQConfig.getRabbitQueue(), rabbitMQConfig.getRabbitExchange(), "#");
        } catch (Exception e) {
            throw new MessagingLoadException("Error while trying to init RabbitMQ connection");
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
                createConsumer(processorName, messageHandler);
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

    @Override
    public void sendMessage(String targetRoutingKey, String message) throws MessagingSendException {
        if (!connectionReady()) {
            initConnection();
        }
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish(exchangeName, targetRoutingKey, null, message.getBytes());
        } catch (Exception e) {
            throw new MessagingSendException("Error while sending message to RabbitMQ");
        }
    }

    /**
     * Register a message processor for a specific queue
     *
     * @param processorName  The queue to consume from
     * @param messageHandler Function that receives ReceivedMessage (routing key + message + ack capability)
     */
    @Override
    public void registerMessageProcessor(String processorName, Consumer<ReceivedMessage> messageHandler) throws MessagingSendException {
        try {
            if (!connectionReady()) {
                initConnection();
            }

            // Store the processor configuration for re-registration after reconnects
            registeredProcessors.put(processorName, messageHandler);

            // Create the actual consumer
            createConsumer(processorName, messageHandler);

            logger.info("Registered message processor for queue: " + processorName);

        } catch (Exception e) {
            throw new MessagingSendException("Error while registering message processor: " + e.getMessage());
        }
    }

    /**
     * Create the actual RabbitMQ consumer using modern DeliverCallback approach
     */
    private void createConsumer(String processorName, Consumer<ReceivedMessage> messageHandler) throws IOException {
        // Create a dedicated channel for this consumer
        Channel consumerChannel = connection.createChannel();

        // Ensure the queue exists
        consumerChannel.queueDeclare(processorName, false, true, true, null);

        // Create the delivery callback
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                String routingKey = delivery.getEnvelope().getRoutingKey();

                logger.debug("Received message from queue '" + processorName + "' with routing key '" + routingKey + "'");

                // Create RabbitMessage wrapper
                RabbitMessage rabbitMessage = new RabbitMessage(consumerChannel, delivery.getEnvelope().getDeliveryTag(), routingKey, message);

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