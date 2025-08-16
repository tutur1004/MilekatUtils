package fr.milekat.utils.messaging;

import fr.milekat.utils.messaging.exceptions.MessagingLoadException;
import fr.milekat.utils.messaging.exceptions.MessagingReceiveException;
import fr.milekat.utils.messaging.exceptions.MessagingSendException;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface MessagingConnection {
    /**
     * Initializes the messaging connection.
     *
     * @throws MessagingLoadException if the connection could not be established
     */
    void initConnection() throws MessagingLoadException;

    /**
     * Checks if the messaging connection is ready.
     *
     * @return true if the connection is ready, false otherwise
     * @throws MessagingLoadException if there was an error checking the connection status
     */
    boolean connectionReady() throws MessagingLoadException;

    /**
     * Closes the messaging connection. It also stops any message processing.
     *
     * @throws MessagingLoadException if there was an error closing the connection
     */
    void close();

    /**
     * Gets the messaging vendor associated with this connection.
     *
     * @return the MessagingVendor enum representing the vendor
     */
    MessagingVendor getVendor();

    /**
     * Sends a message to the specified target.
     * @param target the target identifier (Mostly a routing key or channel name)
     * @param message the message to send
     * @throws MessagingSendException if there was an error sending the message
     */
    void sendMessage(String target, String message) throws MessagingSendException;

    /**
     * Registers a message processor for a specific routing key.
     * This processor will handle incoming messages that match the routing key.
     *
     * @param processorName the name of the message processor (used for identification and management)
     * @param routingKey the routing key to listen for messages
     * @param messageHandler consumer that processes the received messages
     * @throws MessagingSendException if there was an error registering the message processor
     */
    void registerMessageProcessor(String processorName, String routingKey, Consumer<ReceivedMessage> messageHandler) throws MessagingSendException;

    /**
     * Unregisters a message processor by its name.
     * This stops the processor from receiving any further messages.
     *
     * @param processorName the name of the message processor to unregister
     * @throws MessagingReceiveException if there was an error unregistering the message processor
     */
    void unregisterMessageProcessor(String processorName) throws MessagingReceiveException;
}
