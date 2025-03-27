package fr.milekat.utils.storage;

import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import fr.milekat.utils.storage.adapter.elasticsearch.connetion.ESConnection;
import fr.milekat.utils.storage.adapter.sql.connection.SQLConnection;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class StorageLoader {
    public static MileLogger STORAGE_LOGGER = new MileLogger("StorageLoader");
    private final StorageConnection loadedStorage;

    public StorageLoader(@NotNull Configs config, @NotNull MileLogger logger) throws StorageLoadException {
        STORAGE_LOGGER = logger;
        String storageType = config.getString("storage.type");
        STORAGE_LOGGER.debug("Loading storage type: " + storageType);
        switch (storageType.toLowerCase(Locale.ROOT)) {
            case "es":
            case "elastic":
            case "elasticsearch":
                loadedStorage = new ESConnection(config, logger);
                break;
            case "mysql":
            case "mariadb":
            case "postgres":
            case "postgresql":
                loadedStorage = new SQLConnection(config, logger);
                break;
            default:
                throw new StorageLoadException("Unsupported storage type");
        }
        if (loadedStorage.checkStoragesConnection()) {
            STORAGE_LOGGER.debug("Storage loaded");
        } else {
            throw new StorageLoadException("Storages are not loaded properly");
        }
    }

    @SuppressWarnings("unused")
    public StorageConnection getLoadedConnection() {
        return loadedStorage;
    }

    public static MileLogger getStorageLogger() {
        return STORAGE_LOGGER;
    }
}
