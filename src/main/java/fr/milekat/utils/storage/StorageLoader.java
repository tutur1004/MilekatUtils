package fr.milekat.utils.storage;

import fr.milekat.utils.MileLogger;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import fr.milekat.utils.storage.utils.StorageConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public class StorageLoader {
    private static MileLogger storageLogger = new MileLogger("StorageLoader");
    private final StorageConnection loadedStorage;

    public StorageLoader(@NotNull StorageConfig storageConfig, @NotNull MileLogger logger) throws StorageLoadException {
        storageLogger = logger;
        storageLogger.debug("Loading storage type: " + storageConfig.type());
        Map<String, StorageConnection> storageAdapters = StorageAdapterLoader.loadAdapters(storageConfig, logger);

        switch (storageConfig.type().toLowerCase(Locale.ROOT)) {
            case "es":
            case "elastic":
            case "elasticsearch":
                loadedStorage = storageAdapters.get("elasticsearch");
                break;
            case "mysql":
            case "mariadb":
            case "postgres":
            case "postgresql":
                loadedStorage = storageAdapters.get("sql");
                break;
            default:
                throw new StorageLoadException("Unsupported storage type");
        }

        if (loadedStorage.checkStoragesConnection()) {
            storageLogger.debug("Storage loaded");
        } else {
            throw new StorageLoadException("Storages are not loaded properly");
        }
    }

    @SuppressWarnings("unused")
    public StorageConnection getLoadedConnection() {
        return loadedStorage;
    }

    public static MileLogger getStorageLogger() {
        return storageLogger;
    }
}
