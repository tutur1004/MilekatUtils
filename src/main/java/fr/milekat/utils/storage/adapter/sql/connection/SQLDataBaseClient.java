package fr.milekat.utils.storage.adapter.sql.connection;


import fr.milekat.utils.storage.utils.StorageConfig;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public interface SQLDataBaseClient extends AutoCloseable {
    void init(@NotNull StorageConfig storageConfig) throws StorageLoadException;

    boolean isConnected();

    void close();

    Connection getConnection() throws StorageLoadException;
}
