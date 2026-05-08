package fr.milekat.utils.storage.utils;

import fr.milekat.utils.Configs;
import fr.milekat.utils.storage.StorageVendor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Universal storage configuration record for multiple database backends.
 * <p>
 * This immutable configuration supports SQL databases (MySQL, PostgreSQL, etc.),
 * Elasticsearch, and MongoDB, providing a unified interface for connection parameters.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 *
 * <h3>SQL (MySQL/PostgreSQL):</h3>
 * <pre>{@code
 * var config = new StorageConfig(
 *     "sql",                    // type
 *     "myapp_",                 // prefix
 *     null,                     // scheme
 *     null,                     // sslFingerprint
 *     "localhost",              // hostname
 *     "3306",                   // port
 *     "user",                   // username
 *     "pass",                   // password
 *     null,                     // apiKey
 *     "dbName",                 // database
 *     null,                     // collection
 *     Map.of("useSSL", "false") // parameters
 * );
 * }</pre>
 *
 * <h3>Elasticsearch:</h3>
 * <pre>{@code
 * var config = new StorageConfig(
 *     "elastic",                // type
 *     "myapp-",                 // prefix
 *     "https",                  // scheme
 *     "a1:b2:c3...",            // sslFingerprint
 *     "elastic.example.com",    // hostname
 *     "9200",                   // port
 *     "elastic",                // username
 *     "password",               // password
 *     "apiKeyValue",            // apiKey (alternative to username/password)
 *     null,                     // database
 *     null,                     // collection
 *     Map.of("number_of_replicas", "2") // parameters
 * );
 * }</pre>
 *
 * <h3>MongoDB:</h3>
 * <pre>{@code
 * var config = new StorageConfig(
 *     "mongo",                  // type
 *     "myapp_",                 // prefix
 *     "mongodb",                // scheme (or "mongodb+srv")
 *     null,                     // sslFingerprint
 *     "mongo.example.com",      // hostname
 *     "27017",                  // port
 *     "user",                   // username
 *     "pass",                   // password
 *     null,                     // apiKey
 *     "DB",                     // database
 *     "collection",             // collection
 *     Map.of("authSource", "admin") // parameters
 * );
 * }</pre>
 *
 * @param type            The storage backend type (e.g., "sql", "elastic", "mongo")
 * @param prefix          Prefix for table/index/collection names (e.g., "myapp_" or "myapp-")
 * @param scheme          Connection scheme - used for Elasticsearch ("http"/"https") and MongoDB ("mongodb"/"mongodb+srv"), null for SQL
 * @param sslFingerprint  SSL certificate fingerprint for secure Elasticsearch connections, null if not using SSL verification
 * @param hostname        Database/service hostname or IP address
 * @param port            Service port number as string (e.g., "3306" for MySQL, "9200" for Elasticsearch, "27017" for MongoDB)
 * @param username        Authentication username, null if using apiKey or no authentication
 * @param password        Authentication password, null if using apiKey or no authentication
 * @param apiKey          API key for authentication (primarily for Elasticsearch), null if using username/password
 * @param database        Database name - used for SQL and MongoDB, null for Elasticsearch
 * @param collection      MongoDB collection name, null for SQL and Elasticsearch
 * @param parameters      Additional connection parameters as query string (e.g., "useSSL=false"), null if none
 */
@SuppressWarnings("unused")
public record StorageConfig(@NotNull String type, @NotNull String prefix,
                            @Nullable String scheme, @Nullable String sslFingerprint,
                            @NotNull String hostname, @NotNull String port,
                            @Nullable String username, @Nullable String password, @Nullable String apiKey,
                            @Nullable String database, @Nullable String collection,
                            @Nullable Map<String, String> parameters) {

    /**
     * Factory method to create a StorageConfig based on the StorageVendor and Configs instance.
     * @param vendor Storage vendor to create config for
     * @param configs Configs instance to read parameters from
     * @return StorageConfig instance with parameters populated based on the vendor and configs
     */
    public static @NotNull StorageConfig fromVendor(@NotNull StorageVendor vendor, @NotNull Configs configs) {
        return switch (vendor) {
            case MYSQL, MARIADB, POSTGRESQL -> new StorageConfig(
                    "mariadb",
                    configs.getString("storage.sql.prefix", "cryo_"),
                    null,
                    null,
                    configs.getString("storage.sql.hostname", "localhost"),
                    configs.getString("storage.sql.port", "3306"),
                    configs.getString("storage.sql.username", "root"),
                    configs.getString("storage.sql.password", ""),
                    null,
                    configs.getString("storage.sql.database", "cryo"),
                    null,
                    null
            );
            case ELASTICSEARCH -> new StorageConfig(
                    "elastic",
                    configs.getString("storage.elasticsearch.prefix", "cryo-"),
                    configs.getString("storage.elasticsearch.scheme", "http"),
                    configs.getString("storage.elasticsearch.sslFingerprint"),
                    configs.getString("storage.elasticsearch.hostname", "localhost"),
                    configs.getString("storage.elasticsearch.port", "9200"),
                    configs.getString("storage.elasticsearch.username"),
                    configs.getString("storage.elasticsearch.password"),
                    configs.getString("storage.elasticsearch.apiKey"),
                    null,
                    null,
                    Map.of("number_of_replicas",
                            configs.getString("storage.elasticsearch.number_of_replicas", "1"))
            );
        };
    }
}
