package fr.milekat.utils.storage.adapter.sql.connection;

import com.zaxxer.hikari.HikariDataSource;
import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import fr.milekat.utils.storage.StorageConnection;
import fr.milekat.utils.storage.StorageVendor;
import fr.milekat.utils.storage.adapter.sql.hikari.HikariPool;
import fr.milekat.utils.storage.adapter.sql.hikari.MariaDBPool;
import fr.milekat.utils.storage.adapter.sql.hikari.MySQLPool;
import fr.milekat.utils.storage.adapter.sql.hikari.PostgresPool;
import fr.milekat.utils.storage.adapter.sql.utils.Schema;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Locale;

public class SQLConnection implements StorageConnection, AutoCloseable {
    private final MileLogger logger;
    private final String prefix;
    private final SQLDataBaseClient sqlDataBaseClient;
    private final StorageVendor vendor;

    public SQLConnection(@NotNull Configs config, @NotNull MileLogger logger) throws StorageLoadException {
        this.logger = logger;
        prefix = config.getString("storage.prefix");
        HikariPool hikariPool;

        switch (config.getString("storage.type").toLowerCase(Locale.ROOT)) {
            case "mysql":
                hikariPool = new MySQLPool();
                vendor = StorageVendor.MYSQL;
                break;
            case "mariadb":
                hikariPool = new MariaDBPool();
                vendor = StorageVendor.MARIADB;
                break;
            case "postgres":
            case "postgresql":
                hikariPool = new PostgresPool();
                vendor = StorageVendor.POSTGRESQL;
                break;
            default:
                throw new StorageLoadException("Unknown SQL type");
        }

        hikariPool.init(config);
        sqlDataBaseClient = hikariPool;
    }

    @Override
    public boolean checkStoragesConnection() {
        try {
            return sqlDataBaseClient != null && sqlDataBaseClient.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get a client instance of the requested type
     * @param clientClass The type of client to return
     * @param <T> Type parameter for the client
     * @return The client instance
     * @throws UnsupportedOperationException if the requested client type is not supported
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getClient(@NotNull Class<T> clientClass) {
        if (clientClass.equals(SQLDataBaseClient.class)) {
            return (T) sqlDataBaseClient;
        } else if (clientClass.equals(HikariPool.class) && sqlDataBaseClient instanceof HikariPool) {
            return (T) sqlDataBaseClient;
        } else if (clientClass.equals(HikariDataSource.class) && sqlDataBaseClient instanceof HikariPool) {
            return (T) ((HikariPool) sqlDataBaseClient).getDataSource();
        }

        throw new UnsupportedOperationException("Client type " + clientClass.getName() +
                " is not supported by " + vendor.name() + " connection");
    }

    @Override
    public StorageVendor getVendor() {
        return vendor;
    }

    @Override
    public void close() {
        if (sqlDataBaseClient != null) {
            try {
                sqlDataBaseClient.close();
            } catch (Exception e) {
                logger.warning("Error while closing SQL connection: " + e.getMessage());
            }
        }
    }

    /**
     * Loads a database schema from an input stream
     * @param schemaFile InputStream containing the schema SQL
     * @throws StorageLoadException If the schema cannot be loaded
     */
    public void loadSchema(InputStream schemaFile) throws StorageLoadException {
        new Schema(sqlDataBaseClient, schemaFile, prefix);
    }

    /**
     * Gets the table prefix used for this connection
     * @return The table prefix string
     */
    public String getPrefix() {
        return prefix;
    }
}
