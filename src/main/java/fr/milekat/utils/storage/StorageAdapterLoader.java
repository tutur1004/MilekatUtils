package fr.milekat.utils.storage;

import fr.milekat.utils.MileLogger;
import fr.milekat.utils.storage.utils.StorageConfig;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class StorageAdapterLoader {
    public static @NotNull Map<String, StorageConnection>
    loadAdapters(@NotNull StorageConfig storageConfig, @NotNull MileLogger logger) {
        //  Create a map to hold the storage connection instances
        Map<String, StorageConnection> storageConnections = new HashMap<>();
        //  Get the storage vendor from the configuration
        StorageVendor vendor = storageConfig.type();
        try {
            //  Check if the driver class is available
            Class.forName(vendor.getDriverClass());

            //  If yes, create a new StorageConnection instance and add it to the map
            storageConnections.put(vendor.getStorageAdapter(),
                    (StorageConnection) Class.forName(vendor.getAdapterConnectionClass())
                            .getDeclaredConstructor(StorageConfig.class, MileLogger.class)
                            .newInstance(storageConfig, logger));
        } catch (ClassNotFoundException ignored) {
            // Driver not on classpath — adapter intentionally skipped
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException exception) {
            logger.warn("Failed to load storage adapter for " + vendor.name() + ": " + exception.getMessage());
        }
        return storageConnections;
    }
}
