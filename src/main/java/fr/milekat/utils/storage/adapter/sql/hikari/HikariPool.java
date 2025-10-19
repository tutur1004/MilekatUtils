package fr.milekat.utils.storage.adapter.sql.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.milekat.utils.storage.utils.StorageConfig;
import fr.milekat.utils.storage.adapter.sql.connection.SQLDataBaseClient;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract {@link SQLDataBaseClient} using a {@link HikariDataSource}.
 */
public abstract class HikariPool implements SQLDataBaseClient {
    private HikariDataSource hikari;
    private boolean initialized = false;

    @Override
    public void init(@NotNull StorageConfig storageConfig) throws StorageLoadException {
        // If already initialized, close the previous datasource
        if (initialized && hikari != null) {
            hikari.close();
        }

        HikariConfig hikariConfig = new HikariConfig();

        // set pool name so the logging output can be linked back to us
        hikariConfig.setPoolName("infra-hikari");

        // allow the implementation to configure the HikariConfig appropriately with these values
        configureDatabase(hikariConfig,
                storageConfig.hostname(),
                storageConfig.port(),
                storageConfig.database(),
                storageConfig.username(),
                storageConfig.password());

        // configure the connection pool
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setKeepaliveTime(0);
        hikariConfig.setConnectionTimeout(5000);

        // Use a validation query appropriate for the database type
        hikariConfig.setConnectionTestQuery("SELECT 1");

        // Add health check properties
        hikariConfig.addHealthCheckProperty("connectivityCheckTimeoutMs", "1000");
        hikariConfig.addHealthCheckProperty("expected99thPercentileMs", "10");

        this.hikari = new HikariDataSource(hikariConfig);
        this.initialized = true;
    }

    /**
     * Gets the HikariDataSource used by this connection pool.
     * This method is useful for advanced configuration or metrics gathering.
     *
     * @return The HikariDataSource instance
     */
    public HikariDataSource getDataSource() {
        return hikari;
    }

    @Override
    public boolean isConnected() {
        if (hikari == null || hikari.isClosed()) {
            return false;
        }

        try (Connection conn = hikari.getConnection()) {
            return conn != null && conn.isValid(1); // 1 second timeout
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() {
        if (this.hikari != null && !this.hikari.isClosed()) {
            this.hikari.close();
        }
        this.initialized = false;
    }

    @Override
    public Connection getConnection() throws StorageLoadException {
        if (this.hikari == null) {
            throw new StorageLoadException("Unable to get a connection from the pool. (hikari is null)");
        }

        if (this.hikari.isClosed()) {
            throw new StorageLoadException("Unable to get a connection from the pool. (pool is closed)");
        }

        try {
            Connection connection = this.hikari.getConnection();

            if (connection == null) {
                throw new StorageLoadException("Unable to get a connection from the pool. " +
                        "(getConnection returned null)");
            }

            return connection;
        } catch (SQLException e) {
            throw new StorageLoadException("Failed to get connection from the pool: " + e.getMessage(), e);
        }
    }

    /**
     * Configures the {@link HikariConfig} with the relevant database properties.
     *
     * <p>Each driver does this slightly differently...</p>
     *
     * @param config the hikari config
     * @param hostname the database hostname
     * @param port the database port
     * @param databaseName the database name
     * @param username the database username
     * @param password the database password
     * @throws StorageLoadException load exception
     */
    protected abstract void configureDatabase(@NotNull HikariConfig config, String hostname, String port,
                                              String databaseName, String username, String password)
            throws StorageLoadException;
}

