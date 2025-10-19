package fr.milekat.utils.storage.adapter.sql.connection;

import fr.milekat.utils.MileLogger;
import fr.milekat.utils.storage.utils.StorageConfig;
import fr.milekat.utils.storage.StorageConnection;
import fr.milekat.utils.storage.StorageVendor;
import fr.milekat.utils.storage.adapter.sql.hikari.HikariEngine;
import fr.milekat.utils.storage.adapter.sql.hikari.HikariEngineLoaders;
import fr.milekat.utils.storage.adapter.sql.hikari.HikariPool;
import fr.milekat.utils.storage.adapter.sql.utils.Schema;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import fr.milekat.utils.storage.utils.Tools;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public class SQLConnection implements StorageConnection, AutoCloseable {
    private final MileLogger logger;
    private final String prefix;
    private final SQLDataBaseClient sqlDataBaseClient;
    private final StorageVendor vendor;

    public SQLConnection(@NotNull StorageConfig storageConfig, @NotNull MileLogger logger) throws StorageLoadException {
        this.logger = logger;
        prefix = storageConfig.prefix();

        Map<HikariEngine, HikariPool> hikariPools = HikariEngineLoaders.loadHikariPools();

        HikariPool hikariPool;
        switch (storageConfig.type().toLowerCase(Locale.ROOT)) {
            case "mysql":
                hikariPool = hikariPools.get(HikariEngine.MYSQL);
                vendor = StorageVendor.MYSQL;
                break;
            case "mariadb":
                hikariPool = hikariPools.get(HikariEngine.MARIADB);
                vendor = StorageVendor.MARIADB;
                break;
            case "postgres":
            case "postgresql":
                hikariPool = hikariPools.get(HikariEngine.POSTGRES);
                vendor = StorageVendor.POSTGRESQL;
                break;
            default:
                throw new StorageLoadException("Unknown SQL type");
        }

        //  Debug hostname/port
        logger.debug("Hostname: " + storageConfig.hostname());
        logger.debug("Port: " + storageConfig.port());
        logger.debug("Database: " + storageConfig.database());
        logger.debug("Username: " + storageConfig.username());
        if (storageConfig.password() != null && !storageConfig.password().isEmpty()) {
            logger.debug("Password: " + Tools.hideSecret(storageConfig.password()));
        }

        hikariPool.init(storageConfig);
        sqlDataBaseClient = hikariPool;
    }

    /**
     * Checks if the SQL connection is valid
     * @return True if the connection is valid, false otherwise
     */
    @Override
    public boolean checkStoragesConnection() {
        try {
            return sqlDataBaseClient != null && sqlDataBaseClient.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes the SQL connection
     */
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
     * Gets the Vendor of the SQL connection
     * @return The StorageVendor of the connection
     */
    @Override
    public StorageVendor getVendor() {
        return vendor;
    }

    /**
     * Loads a database schema from an input stream
     * @param schemaFile InputStream containing the schema SQL
     * @throws StorageLoadException If the schema cannot be loaded
     */
    public void loadSchema(@NotNull InputStream schemaFile) throws StorageLoadException {
        new Schema(sqlDataBaseClient, schemaFile, prefix);
    }

    /**
     * Gets the table prefix used for this connection
     * @return The table prefix string
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the SQLDataBaseClient used for this connection
     * @return The SQLDataBaseClient
     */
    public SQLDataBaseClient getSQLClient() {
        return sqlDataBaseClient;
    }
}
