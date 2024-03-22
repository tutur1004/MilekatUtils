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

public class SQLConnection implements StorageConnection {
    private final String prefix;
    private final SQLDataBaseConnection sqlDataBaseConnection;
    private final StorageVendor vendor;

    public SQLConnection(@NotNull Configs config) throws StorageLoadException {
        prefix = config.getString("storage.prefix");
        HikariPool hikariPool;
        switch (config.getString("storage.type").toLowerCase()) {
            case "mysql": {
                hikariPool = new MySQLPool();
                vendor = StorageVendor.MYSQL;
                break;
            }
            case "mariadb": {
                hikariPool = new MariaDBPool();
                vendor = StorageVendor.MARIADB;
                break;
            }
            case "postgres": {
                hikariPool = new PostgresPool();
                vendor = StorageVendor.POSTGRESQL;
                break;
            }
            default: {
                throw new StorageLoadException("Unknown SQL type");
            }
        }
        hikariPool.init(config);
        sqlDataBaseConnection = hikariPool;
    }

    @Override
    public boolean checkStoragesConnection() {
        return true;
    }

    public SQLDataBaseConnection getSqlDataBaseConnection() {
        return sqlDataBaseConnection;
    }

    @Override
    public StorageVendor getVendor() {
        return vendor;
    }

    @Override
    public void close() {
        sqlDataBaseConnection.close();
    }

    @Override
    public void loadSchema(InputStream schemaFile) throws StorageLoadException {
        new Schema(sqlDataBaseConnection, schemaFile, prefix);
    }
}
