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
import org.jetbrains.annotations.Contract;
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
    private ElasticsearchClient sharedClient;
    private JacksonJsonpMapper currentMapper;
    private final Object clientLock = new Object();

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
        try {
            ElasticsearchClient client = getEsClient();
            client.cat().indices();
            return true;
        } catch (Exception e) {
            logger.warning("Failed to connect to Elasticsearch: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        synchronized (clientLock) {
            if (sharedClient != null) {
                try {
                    sharedClient._transport().close();
                    logger.info("Elasticsearch client closed.");
                } catch (Exception e) {
                    logger.warning("Error closing Elasticsearch client: " + e.getMessage());
                }
                sharedClient = null;
            }
        }
    }

    /**
     * Get or create the shared Elasticsearch client
     *
     * @return ElasticsearchClient
     */
    public ElasticsearchClient getEsClient() {
        return getEsClient(new JacksonJsonpMapper());
    }

    /**
     * Get or create the shared Elasticsearch client with a custom mapper
     * Note: Changing the mapper will recreate the client
     *
     * @param mapper JacksonJsonMapper
     * @return ElasticsearchClient
     */
    public ElasticsearchClient getEsClient(JacksonJsonpMapper mapper) {
        synchronized (clientLock) {
            if (sharedClient == null || currentMapper != mapper) {
                if (sharedClient != null) {
                    try {
                        sharedClient._transport().close();
                    } catch (Exception e) {
                        logger.debug("Error closing previous client: " + e.getMessage());
                    }
                }

                sharedClient = createNewClient(mapper);
                currentMapper = mapper;
            }

            return sharedClient;
        }
    }

    /**
     * Force reconnection by closing and recreating the client
     */
    public void reconnect() {
        synchronized (clientLock) {
            logger.info("Forcing Elasticsearch reconnection...");
            if (sharedClient != null) {
                try {
                    sharedClient._transport().close();
                } catch (Exception e) {
                    logger.debug("Error closing client during reconnect: " + e.getMessage());
                }
            }
            sharedClient = createNewClient(currentMapper != null ? currentMapper : new JacksonJsonpMapper());
        }
    }

    /**
     * Create a new Elasticsearch client
     */
    @Contract("_ -> new")
    private @NotNull ElasticsearchClient createNewClient(JacksonJsonpMapper mapper) {
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

        logger.debug("Creating new Elasticsearch client");
        return new ElasticsearchClient(transportConfigBuilder.build());
    }

    @Override
    public StorageVendor getVendor() {
        return StorageVendor.ELASTICSEARCH;
    }
}
