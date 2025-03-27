package fr.milekat.utils.storage.adapter.sql.hikari.engines;

import com.zaxxer.hikari.HikariConfig;
import fr.milekat.utils.storage.adapter.sql.hikari.HikariPool;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class MariaDBPool extends HikariPool {
    @Override
    protected void configureDatabase(@NotNull HikariConfig config, String address, String port,
                                     String databaseName, String username, String password)
            throws StorageLoadException {
        try {
            MariaDbDataSource mariaDbDataSource = new MariaDbDataSource();
            mariaDbDataSource.setUrl("jdbc:mariadb://" + address + ":" + port + "/" + databaseName);
            mariaDbDataSource.setUser(username);
            mariaDbDataSource.setPassword(password);
            config.setDataSource(mariaDbDataSource);
        } catch (SQLException exception) {
            throw new StorageLoadException("Error while reading schema file");
        }
    }
}