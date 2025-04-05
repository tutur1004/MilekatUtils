package fr.milekat.utils.storage.adapter.sql.hikari.engines;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import fr.milekat.utils.storage.adapter.sql.hikari.HikariPool;
import org.jetbrains.annotations.NotNull;

public class MySQLPool extends HikariPool {
    @Override
    protected void configureDatabase(@NotNull HikariConfig config, String address, String port,
                                     String databaseName, String username, String password) {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setUrl("jdbc:mysql://" + address + ":" + port + "/" + databaseName);
        mysqlDataSource.setUser(username);
        mysqlDataSource.setPassword(password);
        config.setDataSource(mysqlDataSource);
    }
}
