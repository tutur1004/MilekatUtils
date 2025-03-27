package fr.milekat.utils.messaging;

import fr.milekat.utils.messaging.exceptions.MessagingLoadException;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MessagingConnection {

    boolean checkMessagingConnection() throws MessagingLoadException;

    void close();

    MessagingVendor getVendor();

    default @Nullable String getPrefix() {
        return null;
    }
}
