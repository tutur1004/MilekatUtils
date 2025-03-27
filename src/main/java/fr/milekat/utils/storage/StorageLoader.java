package fr.milekat.utils.storage;

import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public class StorageLoader {
    private static MileLogger storageLogger = new MileLogger("StorageLoader");
    private final StorageConnection loadedStorage;

    public StorageLoader(@NotNull Configs config, @NotNull MileLogger logger) throws StorageLoadException {
        storageLogger = logger;
        String storageType = config.getString("storage.type");
        storageLogger.debug("Loading storage type: " + storageType);

        Map<String, StorageConnection> storageAdapters = StorageAdapterLoader.loadAdapters(config, logger);

        switch (storageType.toLowerCase(Locale.ROOT)) {
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
