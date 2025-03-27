package fr.milekat.utils.storage.adapter.sql.hikari;

public enum HikariEngine {
    MYSQL("MySQLPool", "com.mysql.cj.jdbc.Driver"),
    POSTGRES("PostgresPool", "org.postgresql.Driver"),
    MARIADB("MariaDBPool", "org.mariadb.jdbc.Driver");

    private final String driverClass;
    private final String engineClass;

    HikariEngine(String engineClassName, String driverClass) {
        this.engineClass = "fr.milekat.utils.storage.adapter.sql.hikari.engines." + engineClassName;
        this.driverClass = driverClass;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getEngineClass() {
        return engineClass;
    }
}
