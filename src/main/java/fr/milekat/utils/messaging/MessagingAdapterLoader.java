package fr.milekat.utils.messaging;

import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class MessagingAdapterLoader {
    public static @NotNull Map<String, MessagingConnection>
    loadAdapters(@NotNull Configs config, @NotNull MileLogger logger) {
        //  Create a map to hold the messaging connection instances
        Map<String, MessagingConnection> messagingConnections = new HashMap<>();
        //  Iterate over all MessagingVendor values and create a new MessagingConnection for each one
        for (MessagingVendor vendor : MessagingVendor.values()) {
            //  Check if the messaging adapter is already in the map, if so, skip it
            if (messagingConnections.containsKey(vendor.getMessagingAdapter())) {
                continue;
            }

            try {
                //  Check if the driver class is available
                Class.forName(vendor.getDriverClass());

                //  If yes, create a new MessagingConnection instance and add it to the map
                messagingConnections.put(vendor.getMessagingAdapter(),
                        (MessagingConnection) Class.forName(vendor.getAdapterConnectionClass())
                                .getDeclaredConstructor(Configs.class, MileLogger.class)
                                .newInstance(config, logger));
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException ignored) {}
        }
        return messagingConnections;
    }
}
