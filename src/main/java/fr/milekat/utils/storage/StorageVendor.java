package fr.milekat.utils.storage;

public enum StorageVendor {
    MYSQL("sql", "SQLConnection", "com.mysql.cj.jdbc.Driver"),
    MARIADB("sql", "SQLConnection", "org.mariadb.jdbc.Driver"),
    POSTGRESQL("sql", "SQLConnection", "org.postgresql.Driver"),
    ELASTICSEARCH("elasticsearch", "ESConnection",
            "org.elasticsearch.client.RestClient"),
    ;

    private final String storageAdapter;
    private final String adapterConnectionClass;
    private final String driverClass;

    StorageVendor(String storageAdapter, String adapterConnectionClass, String driverClass) {
        this.storageAdapter = storageAdapter;
        this.adapterConnectionClass = "fr.milekat.utils.storage.adapter." +
                storageAdapter + ".connection." + adapterConnectionClass;
        this.driverClass = driverClass;
    }

    public String getStorageAdapter() {
        return storageAdapter;
    }

    public String getAdapterConnectionClass() {
        return adapterConnectionClass;
    }

    public String getDriverClass() {
        return driverClass;
    }
}
