package fr.milekat.utils.messaging;

import fr.milekat.utils.messaging.exceptions.MessagingLoadException;
import fr.milekat.utils.messaging.exceptions.MessagingReceiveException;
import fr.milekat.utils.messaging.exceptions.MessagingSendException;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MessagingConnection {

    boolean checkMessagingConnection() throws MessagingLoadException;

    void close();

    MessagingVendor getVendor();

    default @Nullable String getPrefix() {
        return null;
    }

    void sendMessage(String target, String message) throws MessagingSendException;

    void registerMessageProcessor(Runnable messageProcessor) throws MessagingReceiveException;
}
