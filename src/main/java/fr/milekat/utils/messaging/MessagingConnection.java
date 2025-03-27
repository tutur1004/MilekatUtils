package fr.milekat.utils.messaging;

import fr.milekat.utils.messaging.exceptions.MessagingLoadException;

public interface MessagingConnection {
    MessagingVendor getVendor();

    void close();

    boolean checkMessagingConnection() throws MessagingLoadException;


}
