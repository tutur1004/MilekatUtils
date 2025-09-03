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

/**
 * RabbitMQ implementation of the MessagingConnection interface.
 *
 * <p>This class provides a robust RabbitMQ connection with the following features:
 * <ul>
 *   <li>Automatic connection recovery and heartbeat management</li>
 *   <li>JSON message format with custom tagging for message filtering</li>
 *   <li>Consumer auto-recovery after connection failures</li>
 *   <li>Thread-safe consumer management with concurrent collections</li>
 *   <li>Automatic message acknowledgment handling with fallback rejection</li>
 * </ul>
 *
 * <p><strong>Configuration Requirements:</strong>
 * The following configuration keys must be present in the config.yml:
 * <pre>
 * messaging:
 *   rabbitmq:
 *     hostname: "localhost"          # RabbitMQ server hostname
 *     port: 5672                     # RabbitMQ server port (optional, defaults to 5672)
 *     vhost: "/"                     # Virtual host (optional, defaults to "/")
 *     username: "guest"              # RabbitMQ username
 *     password: "guest"              # RabbitMQ password
 *     exchange: "milekat.exchange"   # Exchange name (optional, defaults to "milekat.exchange")
 *     type: "x-rtopic"               # Exchange type (optional, defaults to "x-rtopic")
 * </pre>
 *
 * <p><strong>Message Format:</strong>
 * All messages are sent in JSON format with the following structure:
 * <pre>
 * {
 *   "TAG": "JSON_MESSAGE",           # Custom tag for message filtering
 *   "senderCallBackKey": "callback", # Routing key for reply messages
 *   "message": "actual content"      # The actual message payload
 * }
 * </pre>
 *
 * <p><strong>Consumer Management:</strong>
 * Consumers are automatically re-registered after connection failures. Each consumer:
 * <ul>
 *   <li>Gets its own dedicated channel for isolation</li>
 *   <li>Only processes messages with the correct TAG</li>
 *   <li>Automatically acknowledges or rejects messages based on handler behavior</li>
 *   <li>Handles JSON parsing errors gracefully by acknowledging invalid messages</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong>
 * This class is thread-safe for concurrent message sending and consumer registration.
 * Connection initialization is synchronized to prevent race conditions.
 *
 * <p><strong>Usage Example:</strong>
 * <pre>
 * RabbitMQConnection connection = new RabbitMQConnection(config, logger);
 *
 * // Send a message
 * connection.sendMessage("target.routing.key", "callback.routing.key", "Hello World");
 *
 * // Register a consumer
 * connection.registerMessageProcessor("myQueue", "routing.key.*", message -> {
 *     System.out.println("Received: " + message.getMessage());
 *     message.acknowledge();
 * });
 * </pre>
 *
 * @author MileKat
 * @since 1.6
 */
@SuppressWarnings("unused")
public class RabbitMQConnection implements MessagingConnection {

    /** Logger instance for debugging and monitoring connection activities */
    private final MileLogger logger;

    /** RabbitMQ connection factory with configured connection parameters */
    private final ConnectionFactory connectionFactory;

    /** Messaging channel configuration containing exchange name and type */
    private final MessagingChanel rabbitMQConfig;

    /**
     * Custom message tag used to filter and identify messages sent by this application.
     * Only messages with this tag will be processed by consumers.
     */
    private final String messageTag = "JSON_MESSAGE";

    /**
     * Thread-safe map tracking active consumers by their consumer tags.
     * Used for proper cleanup and channel management.
     * Key: Consumer tag, Value: Channel instance
     */
    private final ConcurrentMap<String, Channel> activeConsumers = new ConcurrentHashMap<>();

    /**
     * Thread-safe map storing consumer configurations for automatic re-registration after reconnection.
     * Enables automatic recovery of all consumers when the connection is restored.
     * Key: Processor name, Value: A Pair of routing key and message handler
     */
    private final ConcurrentMap<String, Map.Entry<String, Consumer<ReceivedMessage>>> registeredProcessors = new ConcurrentHashMap<>();

    /**
     * The main RabbitMQ connection instance. Marked volatile for thread-safe visibility
     * across multiple threads during connection state changes.
     */
    private volatile Connection connection;

    /**
     * Creates a new RabbitMQ connection with the provided configuration.
     *
     * <p>This constructor:
     * <ol>
     *   <li>Reads RabbitMQ connection parameters from the configuration</li>
     *   <li>Sets up the connection factory with automatic recovery enabled</li>
     *   <li>Establishes the initial connection to the RabbitMQ server</li>
     *   <li>Configures heartbeat and network recovery settings for reliability</li>
     * </ol>
     *
     * <p><strong>Connection Settings:</strong>
     * <ul>
     *   <li>Automatic Recovery: Enabled with 5-second intervals</li>
     *   <li>Heartbeat: 30 seconds to detect connection issues</li>
     *   <li>Network Recovery: Automatically reconnects on network failures</li>
     * </ul>
     *
     * @param config Configuration object containing RabbitMQ connection parameters
     * @param logger Logger instance for debugging and monitoring
     * @throws MessagingLoadException if unable to establish connection to RabbitMQ server
     *
     * @see Configs
     * @see MileLogger
     */
    public RabbitMQConnection(@NotNull Configs config, @NotNull MileLogger logger) {
        this.logger = logger;

        // Fetch connections vars from config.yml file
        String host = config.getString("messaging.rabbitmq.hostname");
        int port = config.getInt("messaging.rabbitmq.port", 5672);
        String vhost = config.getString("messaging.rabbitmq.vhost", "/");
        String username = config.getString("messaging.rabbitmq.username", "null");
        String password = config.getString("messaging.rabbitmq.password", "null");

        // Debug hostname/port
        logger.debug("Hostname: " + host);
        logger.debug("Port: " + port);
        logger.debug("Username: " + username);
        logger.debug("Password: " + new String(new char[password.length()]).replace("\0", "*"));

        // Get RabbitMQ configuration from config.yml
        this.rabbitMQConfig = new MessagingChanel(
                config.getString("messaging.rabbitmq.exchange", "milekat.exchange"),
                config.getString("messaging.rabbitmq.type", "x-rtopic")
        );

        // Init the connection factory
        this.connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setVirtualHost(vhost);
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

    /**
     * Initializes or re-initializes the RabbitMQ connection.
     *
     * <p>This method is synchronized to prevent multiple threads from creating
     * connections simultaneously. It performs the following operations:
     * <ol>
     *   <li>Closes any existing connection gracefully</li>
     *   <li>Clears the active consumers map (consumers will be re-created)</li>
     *   <li>Creates a new connection using the configured connection factory</li>
     *   <li>Tests the connection by creating a temporary channel</li>
     *   <li>Re-registers all previously registered consumers for automatic recovery</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong>
     * If connection creation fails, this method throws a MessagingLoadException
     * with details about the failure cause.
     *
     * @throws MessagingLoadException if connection cannot be established or channel creation fails
     *
     * @see #reRegisterAllConsumers()
     */
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
     * Re-registers all previously registered consumers after a reconnection.
     *
     * <p>This method is crucial for automatic recovery functionality. When the connection
     * is restored, all consumers that were active before the disconnection are automatically
     * recreated with their original configurations.
     *
     * <p>The method iterates through all stored consumer configurations and attempts to
     * recreate each consumer. If a consumer fails to re-register, it logs a warning but
     * continues with the remaining consumers to maximize service availability.
     *
     * <p><strong>Recovery Process:</strong>
     * <ol>
     *   <li>Checks if there are any consumers to re-register</li>
     *   <li>Logs the number of consumers being recovered</li>
     *   <li>Attempts to recreate each consumer using stored configuration</li>
     *   <li>Logs success/failure for each consumer recovery attempt</li>
     * </ol>
     *
     * @see #createConsumer(String, String, Consumer)
     * @see #registeredProcessors
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

    /**
     * Checks if the RabbitMQ connection is ready and healthy.
     *
     * <p>This method performs a comprehensive health check by:
     * <ol>
     *   <li>Verifying the connection object exists and is open</li>
     *   <li>Attempting to create a test channel to confirm connection health</li>
     *   <li>Automatically cleaning up the test channel</li>
     * </ol>
     *
     * <p>The channel creation test is important because a connection might appear
     * open but be unable to create channels due to authentication issues, resource
     * limits, or other server-side problems.
     *
     * @return true if connection is healthy and can create channels, false otherwise
     *
     * @see Connection#isOpen()
     * @see Connection#createChannel()
     */
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

    /**
     * Gracefully closes the RabbitMQ connection and cleans up all resources.
     *
     * <p>This method ensures proper cleanup by:
     * <ol>
     *   <li>Clearing all active consumers from the tracking map</li>
     *   <li>Removing all registered processors to prevent re-registration</li>
     *   <li>Closing the main connection if it exists and is open</li>
     *   <li>Logging the closure status for monitoring purposes</li>
     * </ol>
     *
     * <p><strong>Resource Cleanup:</strong>
     * Individual consumer channels are automatically closed when the main connection
     * closes, so explicit channel cleanup is not necessary here.
     *
     * <p><strong>Error Handling:</strong>
     * If connection closure fails, the error is logged as a warning but doesn't
     * throw an exception to allow application shutdown to continue.
     *
     * @see Connection#close()
     */
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

    /**
     * Returns the messaging vendor type for this connection.
     *
     * @return MessagingVendor.RABBITMQ indicating this is a RabbitMQ implementation
     */
    @Override
    public MessagingVendor getVendor() {
        return MessagingVendor.RABBITMQ;
    }

    /**
     * Sends a message to the specified routing key using JSON format with callback support.
     *
     * <p>This method creates a JSON-wrapped message containing:
     * <ul>
     *   <li>TAG: Custom message tag for filtering</li>
     *   <li>senderCallBackKey: Routing key for reply messages</li>
     *   <li>message: The actual message content</li>
     * </ul>
     *
     * <p><strong>Connection Handling:</strong>
     * If the connection is not ready, this method automatically attempts to
     * re-establish it before sending the message.
     *
     * <p><strong>Channel Management:</strong>
     * Uses a try-with-resources block to ensure the channel is properly closed
     * after message publication, preventing resource leaks.
     *
     * <p><strong>Message Format Example:</strong>
     * <pre>
     * {
     *   "TAG": "JSON_MESSAGE",
     *   "senderCallBackKey": "callback.routing.key",
     *   "message": "Hello, World!"
     * }
     * </pre>
     *
     * @param targetRoutingKey The routing key where the message will be sent
     * @param senderCallBackKey The routing key for callback/reply messages (can be null)
     * @param message The actual message content to send
     * @throws MessagingSendException if JSON creation fails or message publishing fails
     *
     * @see #connectionReady()
     * @see #initConnection()
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

            channel.basicPublish(rabbitMQConfig.getName(), targetRoutingKey, null,
                    jsonString.getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            throw new MessagingSendException("Error while creating JSON message: " + e.getMessage());
        } catch (Exception e) {
            throw new MessagingSendException("Error while sending message to RabbitMQ: " + e.getMessage());
        }
    }

    /**
     * Registers a message processor for consuming messages from a specific queue.
     *
     * <p>This method sets up a durable consumer that:
     * <ul>
     *   <li>Creates and binds a queue to the configured exchange</li>
     *   <li>Filters messages by the custom TAG to process only application messages</li>
     *   <li>Handles JSON parsing and validation automatically</li>
     *   <li>Provides automatic acknowledgment fallback for unhandled messages</li>
     *   <li>Stores configuration for automatic re-registration after reconnection</li>
     * </ul>
     *
     * <p><strong>Queue Configuration:</strong>
     * <ul>
     *   <li>Durable: false (queue doesn't survive broker restart)</li>
     *   <li>Exclusive: true (only this connection can use the queue)</li>
     *   <li>Auto-delete: true (queue is deleted when connection closes)</li>
     * </ul>
     *
     * <p><strong>Message Processing Flow:</strong>
     * <ol>
     *   <li>Receive raw message from RabbitMQ</li>
     *   <li>Parse as JSON and validate TAG field</li>
     *   <li>Extract callback routing key and actual message content</li>
     *   <li>Create ReceivedMessage wrapper with acknowledgment capabilities</li>
     *   <li>Call user-provided message handler</li>
     *   <li>Auto-reject if handler didn't acknowledge or reject the message</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong>
     * <ul>
     *   <li>Non-JSON messages are acknowledged and ignored</li>
     *   <li>Messages without correct TAG are acknowledged and ignored</li>
     *   <li>Handler exceptions result in message rejection without requeue</li>
     *   <li>Unhandled messages are automatically rejected with a warning</li>
     * </ul>
     *
     * @param processorName Unique name for the processor (used as queue name)
     * @param routingKey Routing key pattern for binding the queue to the exchange
     * @param messageHandler Function to process received messages
     * @throws MessagingLoadException if processor name already exists or connection fails
     *
     * @see #createConsumer(String, String, Consumer)
     * @see ReceivedMessage
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
        } catch (IOException e) {
            logger.stack(e.getStackTrace());
            throw new MessagingLoadException("IO Error while registering message processor: " + e.getMessage());
        }
    }

    /**
     * Creates the actual RabbitMQ consumer using modern DeliverCallback approach.
     *
     * <p>This method handles the low-level consumer setup including:
     * <ul>
     *   <li>Creating a dedicated channel for consumer isolation</li>
     *   <li>Declaring and binding the queue to the exchange</li>
     *   <li>Setting up message delivery and cancellation callbacks</li>
     *   <li>Implementing robust message processing with error handling</li>
     *   <li>Tracking the consumer for proper resource management</li>
     * </ul>
     *
     * <p><strong>Channel Isolation:</strong>
     * Each consumer gets its own channel to prevent interference between
     * different consumers and to allow independent flow control.
     *
     * <p><strong>Message Validation:</strong>
     * Only processes messages that are valid JSON and contain the correct TAG.
     * Invalid or foreign messages are acknowledged and discarded to keep
     * the queue clean.
     *
     * <p><strong>Acknowledgment Strategy:</strong>
     * Uses manual acknowledgment mode to ensure message reliability.
     * Messages are only removed from the queue after successful processing
     * or explicit rejection.
     *
     * @param processorName Name of the processor (used as queue name)
     * @param routingKey Routing key pattern for queue binding
     * @param messageHandler User-provided message processing function
     * @throws IOException if queue setup or consumer creation fails
     *
     * @see DeliverCallback
     * @see CancelCallback
     * @see RabbitMessage
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
     * Unregisters a message processor, stopping message consumption and removing it from auto-recovery.
     *
     * <p>This method performs complete cleanup by:
     * <ol>
     *   <li>Removing the processor from the registered processors map</li>
     *   <li>Finding and stopping the corresponding consumer</li>
     *   <li>Closing the consumer's dedicated channel</li>
     *   <li>Removing the consumer from active consumers tracking</li>
     * </ol>
     *
     * <p><strong>Resource Cleanup:</strong>
     * The method ensures that all resources associated with the processor are
     * properly cleaned up, preventing memory leaks and unused connections.
     *
     * <p><strong>Error Handling:</strong>
     * If channel closure fails, the error is logged but the cleanup continues
     * to ensure the processor is fully removed from tracking maps.
     *
     * @param processorName The name of the processor to unregister
     *
     * @see #registerMessageProcessor(String, String, Consumer)
     */
    @Override
    public void unregisterMessageProcessor(String processorName) {
        registeredProcessors.remove(processorName);

        // Find and stop the corresponding consumer
        activeConsumers.entrySet().removeIf(entry -> {
            try {
                Channel channel = entry.getValue();
                if (channel.isOpen()) {
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