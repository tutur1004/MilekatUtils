package fr.milekat.utils.storage.adapter.sql.connection;

import fr.milekat.utils.Configs;
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
    private final String prefix;
    private final SQLDataBaseClient sqlDataBaseClient;
    private final StorageVendor vendor;

    public SQLConnection(@NotNull Configs config) throws StorageLoadException {
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
        return true;
    }

    public SQLDataBaseClient getSQLClient() {
        return sqlDataBaseClient;
    }

    @Override
    public StorageVendor getVendor() {
        return vendor;
    }

    @Override
    public void close() {
        sqlDataBaseClient.close();
    }

    @Override
    public void loadSchema(InputStream schemaFile) throws StorageLoadException {
        new Schema(sqlDataBaseClient, schemaFile, prefix);
    }
}
