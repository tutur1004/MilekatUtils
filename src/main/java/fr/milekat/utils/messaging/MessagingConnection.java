package fr.milekat.utils.messaging;

import fr.milekat.utils.messaging.exceptions.MessagingLoadException;
import fr.milekat.utils.messaging.exceptions.MessagingReceiveException;
import fr.milekat.utils.messaging.exceptions.MessagingSendException;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface MessagingConnection {
    /**
     * Initializes the messaging connection.
     *
     * @throws MessagingLoadException if the connection could not be established
     */
    void initConnection() throws MessagingLoadException;

    boolean connectionReady() throws MessagingLoadException;

    void close();

    MessagingVendor getVendor();

    default @Nullable String getPrefix() {
        return null;
    }

    void sendMessage(String target, String message) throws MessagingSendException;

    void registerMessageProcessor(Runnable messageProcessor) throws MessagingReceiveException;
    void registerMessageProcessor(String processorName, Consumer<ReceivedMessage> messageHandler) throws MessagingReceiveException;
}
