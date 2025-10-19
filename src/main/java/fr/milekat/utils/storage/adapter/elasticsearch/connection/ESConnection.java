package fr.milekat.utils.storage.adapter.elasticsearch.connection;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransportConfig;
import co.elastic.clients.transport.TransportUtils;
import fr.milekat.utils.MileLogger;
import fr.milekat.utils.storage.utils.Tools;
import fr.milekat.utils.storage.utils.StorageConfig;
import fr.milekat.utils.storage.StorageConnection;
import fr.milekat.utils.storage.StorageVendor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ESConnection implements StorageConnection, AutoCloseable {
    private final MileLogger logger;
    private final String schema;
    private final String hostname;
    private final int port;
    private final String apiKey;
    private final String username;
    private final String password;
    private final String sslFingerprint;

    public ESConnection(@NotNull StorageConfig storageConfig, @NotNull MileLogger logger) {
        this.logger = logger;
        //  Fetch connections vars from config.yml file
        schema = storageConfig.scheme();
        hostname = storageConfig.hostname();
        port = Integer.parseInt(storageConfig.port());
        apiKey = storageConfig.apiKey();
        username = storageConfig.username();
        password = storageConfig.password();
        sslFingerprint = storageConfig.sslFingerprint();
        //  Debug hostname/port
        logger.debug("Hostname: " + hostname);
        logger.debug("Port: " + port);
        logger.debug("Username: " + username);
        if (password != null && !password.isEmpty()) {
            logger.debug("Password: " + Tools.hideSecret(password));
        }
        if (apiKey != null && !apiKey.isEmpty()) {
            logger.debug("API Key: " + Tools.hideSecret(apiKey));
        }
        if (sslFingerprint != null && !sslFingerprint.isEmpty()) {
            logger.debug("SSL Fingerprint: " + sslFingerprint);
        }
    }

    @Override
    public boolean checkStoragesConnection() {
        // Initialize connection to test it
        try {
            getEsClient();
            // You could add more specific checks here, like a ping request
            return true;
        } catch (Exception e) {
            logger.warning("Failed to connect to Elasticsearch: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        logger.info("Elasticsearch connection closed.");
    }

    @Override
    public StorageVendor getVendor() {
        return StorageVendor.ELASTICSEARCH;
    }

    /**
     * Get the Elasticsearch client
     *
     * @return ElasticsearchClient
     */
    @SuppressWarnings("UnusedReturnValue")
    public ElasticsearchClient getEsClient() {
        return getEsClient(new JacksonJsonpMapper());
    }

    /**
     * Get the Elasticsearch client with a custom JacksonJsonMapper
     *
     * @param mapper JacksonJsonMapper
     * @return ElasticsearchClient
     */
    public ElasticsearchClient getEsClient(JacksonJsonpMapper mapper) {
        ElasticsearchTransportConfig.Builder transportConfigBuilder = new ElasticsearchTransportConfig.Builder();
        transportConfigBuilder.host(schema + "://" + hostname + ":" + port);

        if (sslFingerprint != null && !sslFingerprint.isEmpty()) {
            // Use SSL fingerprint for secure connection
            transportConfigBuilder.sslContext(TransportUtils.sslContextFromCaFingerprint(sslFingerprint));
        } else {
            // Use insecure SSL context if no fingerprint is provided
            transportConfigBuilder.sslContext(TransportUtils.insecureSSLContext());
        }

        if (apiKey != null && !apiKey.isEmpty()) {
            // Use API key for authentication
            transportConfigBuilder.apiKey(apiKey);
        } else if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            // Use username and password for authentication
            transportConfigBuilder.usernameAndPassword(username, password);
        }

        if (mapper != null) {
            transportConfigBuilder.jsonMapper(mapper);
        }

        return new ElasticsearchClient(transportConfigBuilder.build());
    }
}

