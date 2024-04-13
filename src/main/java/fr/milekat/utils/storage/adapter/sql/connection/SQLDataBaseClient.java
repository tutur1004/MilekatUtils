package fr.milekat.utils.storage.adapter.sql.connection;


import fr.milekat.utils.Configs;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLDataBaseClient extends AutoCloseable {
    void init(@NotNull Configs configs) throws StorageLoadException;

    void close();

    Connection getConnection() throws SQLException;
}
