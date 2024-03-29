package fr.milekat.utils.storage.adapter.sql.hikari;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.postgresql.ds.PGSimpleDataSource;

public class PostgresPool extends HikariPool {
    @Override
    protected void configureDatabase(@NotNull HikariConfig config, String address, String port,
                                     String databaseName, String username, String password) {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setServerNames(new String[] {address});
        pgSimpleDataSource.setPortNumbers(new int[] {Integer.parseInt(port)});
        pgSimpleDataSource.setDatabaseName(databaseName);
        pgSimpleDataSource.setUser(username);
        pgSimpleDataSource.setPassword(password);
        config.setDataSource(pgSimpleDataSource);
    }
}
