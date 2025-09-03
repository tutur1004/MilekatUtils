package fr.milekat.utils.storage;

import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class StorageAdapterLoader {
    public static @NotNull Map<String, StorageConnection>
    loadAdapters(@NotNull Configs config, @NotNull MileLogger logger) {
        //  Create a map to hold the storage connection instances
        Map<String, StorageConnection> storageConnections = new HashMap<>();
        //  Iterate over all StorageVendor values and create a new StorageConnection for each one
        for (StorageVendor vendor : StorageVendor.values()) {
            //  Check if the storage adapter is already in the map, if so, skip it
            if (storageConnections.containsKey(vendor.getStorageAdapter())) {
                continue;
            }

            try {
                //  Check if the driver class is available
                Class.forName(vendor.getDriverClass());

                //  If yes, create a new StorageConnection instance and add it to the map
                storageConnections.put(vendor.getStorageAdapter(),
                        (StorageConnection) Class.forName(vendor.getAdapterConnectionClass())
                                .getDeclaredConstructor(Configs.class, MileLogger.class)
                                .newInstance(config, logger));
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException ignored) {}
        }
        return storageConnections;
    }
}
