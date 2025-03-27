package fr.milekat.utils.messaging.adapter.rabbitmq.connection;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import fr.milekat.utils.messaging.MessagingConnection;
import fr.milekat.utils.messaging.MessagingVendor;
import fr.milekat.utils.messaging.exceptions.MessagingLoadException;
import fr.milekat.utils.messaging.exceptions.MessagingSendException;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class RabbitMQConnection implements MessagingConnection {
    private final MileLogger logger;
    private final ConnectionFactory connectionFactory;
    private final String exchangeName;

    public RabbitMQConnection(@NotNull Configs config, @NotNull MileLogger logger) {
        this.logger = logger;
        //  Fetch connections vars from config.yml file
        String host = config.getString("messaging.rabbitmq.hostname");
        int port = config.getInt("messaging.rabbitmq.port", 5672);
        String username = config.getString("messaging.rabbitmq.username", "null");
        String password = config.getString("messaging.rabbitmq.password", "null");
        exchangeName = config.getString("messaging.rabbitmq.exchange-name", "null");
        //  Debug hostname/port
        logger.debug("Hostname: " + host);
        logger.debug("Port: " + port);
        logger.debug("Username: " + username);
        logger.debug("Password: " + new String(new char[password.length()]).replace("\0", "*"));
        logger.debug("Exchange name: " + exchangeName);
        //  Init the connection
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        initConnection();
        //  Check if the connection is valid
        try {
            checkMessagingConnection();
        } catch (MessagingLoadException e) {
            throw new MessagingLoadException("Couldn't connect to RabbitMQ server");
        }
    }

    private void initConnection() {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            // TODO: Write this
//            channel.exchangeDeclare(MessagingVendor.RABBITMQ.getExchangeName(), "topic");
//            channel.queueDeclare(MessagingVendor.RABBITMQ.getQueueName(), true, false, false, null);
//            channel.queueBind(MessagingVendor.RABBITMQ.getQueueName(), MessagingVendor.RABBITMQ.getExchangeName(), "#");)
        } catch (Exception e) {
            throw new MessagingLoadException("Error while trying to init RabbitMQ connection");
        }
    }

    @Override
    public boolean checkMessagingConnection() throws MessagingLoadException {
        try (Connection connection = this.connectionFactory.newConnection();
             Channel ignored = connection.createChannel()) {
            return true;
        } catch (Exception exception) {
            throw new MessagingLoadException("Error while trying to check RabbitMQ connection");
        }
    }

    @Override
    public void close() {
        logger.info("Closing RabbitMQ connection");
    }

    @Override
    public MessagingVendor getVendor() {
        return MessagingVendor.RABBITMQ;
    }

    @Override
    public void sendMessage(String target, String message) throws MessagingSendException {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.basicPublish(exchangeName, target, null, message.getBytes());
        } catch (Exception e) {
            throw new MessagingSendException("Error while sending message to RabbitMQ");
        }
    }

    @Override
    public void registerMessageProcessor(Runnable messageProcessor) throws MessagingSendException {
        //  TODO: Implement message processor with RabbitMQConsumer
    }
}
