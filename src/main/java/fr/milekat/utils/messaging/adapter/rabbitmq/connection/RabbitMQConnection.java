package fr.milekat.utils.messaging.adapter.rabbitmq.connection;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import fr.milekat.utils.messaging.MessagingConnection;
import fr.milekat.utils.messaging.MessagingVendor;
import fr.milekat.utils.messaging.adapter.rabbitmq.RabbitMQConfigProvider;
import fr.milekat.utils.messaging.exceptions.MessagingLoadException;
import fr.milekat.utils.messaging.exceptions.MessagingSendException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("unused")
public class RabbitMQConnection implements MessagingConnection {
    private final MileLogger logger;
    private final ConnectionFactory connectionFactory;
    private final RabbitMQConfigProvider rabbitMQConfig;

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
            } catch (Exception ignored) {}
        }

        try {
            connection = connectionFactory.newConnection();
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
    }

    @Override
    public boolean connectionReady() {
        if (connection == null || !connection.isOpen()) {
            return false;
        }

        // Try to create a channel to verify connection health
        try (Channel channel = connection.createChannel()) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() {
        logger.info("Closing RabbitMQ connection");
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

    @Override
    public void registerMessageProcessor(Runnable messageProcessor) throws MessagingSendException {
        // TODO: Implement message processor with RabbitMQConsumer
    }
}