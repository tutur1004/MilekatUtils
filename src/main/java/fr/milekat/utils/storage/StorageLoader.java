package fr.milekat.utils.storage;

import fr.milekat.utils.MileLogger;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import fr.milekat.utils.storage.utils.StorageConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public class StorageLoader {
    private static MileLogger storageLogger = new MileLogger("StorageLoader");
    private final StorageConnection loadedStorage;

    public StorageLoader(@NotNull StorageConfig storageConfig, @NotNull MileLogger logger) throws StorageLoadException {
        storageLogger = logger;
        storageLogger.debug("Loading storage type: " + storageConfig.type().name());
        loadedStorage = this.loadAdapter(storageConfig, logger);
        if  (loadedStorage == null) {
            throw new StorageLoadException("Failed to load storage type: " + storageConfig.type().name());
        }
    }

    @SuppressWarnings("unused")
    public StorageConnection getLoadedConnection() {
        return loadedStorage;
    }

    public static MileLogger getStorageLogger() {
        return storageLogger;
    }

    private @Nullable StorageConnection loadAdapter(@NotNull StorageConfig storageConfig, @NotNull MileLogger logger) {
        //  Get the storage vendor from the configuration
        StorageVendor vendor = storageConfig.type();
        try {
            //  Check if the driver class is available
            Class.forName(vendor.getDriverClass());

            //  If yes, create a new StorageConnection instance and add it to the map
            return (StorageConnection) Class.forName(vendor.getAdapterConnectionClass())
                    .getDeclaredConstructor(StorageConfig.class, MileLogger.class)
                    .newInstance(storageConfig, logger);
        } catch (ClassNotFoundException ignored) {
            logger.warn("Storage adapter for " + vendor.name() + " is not available. " +
                    "Missing driver: " + vendor.getDriverClass());
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException exception) {
            logger.warn("Failed to load storage adapter for " + vendor.name() + ": " + exception.getMessage());
        }
        return null;
    }
}
