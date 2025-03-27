package fr.milekat.utils.storage.adapter.elasticsearch.connetion;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import fr.milekat.utils.storage.StorageConnection;
import fr.milekat.utils.storage.StorageVendor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ESConnection implements StorageConnection, AutoCloseable {
    private final MileLogger logger;
    private RestClient restClient;
    private RestClientTransport transport;
    private ElasticsearchClient elasticsearchClient;
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    public ESConnection(@NotNull Configs config, @NotNull MileLogger logger) {
        this.logger = logger;
        //  Fetch connections vars from config.yml file
        hostname = config.getString("storage.elasticsearch.hostname");
        port = config.getInt("storage.elasticsearch.port", 9200);
        username = config.getString("storage.elasticsearch.username", "null");
        password = config.getString("storage.elasticsearch.password", "null");
        //  Debug hostname/port
        logger.debug("Hostname: " + hostname);
        logger.debug("Port: " + port);
        logger.debug("Username: " + username);
        logger.debug("Password: " + new String(new char[password.length()]).replace("\0", "*"));
    }

    private @NotNull RestClient getRestClient() {
        //  Init the RestClientBuilder
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(hostname, port));
        //  If credentials are set, apply credentials to RestClientBuilder
        if (!username.equals("null") && !password.equals("null")) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            restClientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }
        return restClientBuilder.build();
    }

    /**
     * Initialize the Elasticsearch client if not already done
     */
    private void initializeClientIfNeeded() {
        if (this.elasticsearchClient == null) {
            this.restClient = getRestClient();
            this.transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            this.elasticsearchClient = new ElasticsearchClient(transport);
        }
    }

    @Override
    public StorageVendor getVendor() {
        return StorageVendor.ELASTICSEARCH;
    }

    @Override
    public void close() {
        try {
            if (transport != null) {
                transport.close();
            }
            if (restClient != null) {
                restClient.close();
            }
            // Reset references
            this.elasticsearchClient = null;
            this.transport = null;
            this.restClient = null;
        } catch (IOException exception) {
            logger.warning("Error while closing Elasticsearch connection: " + exception.getMessage());
        }
    }

    @Override
    public boolean checkStoragesConnection() {
        // Initialize connection to test it
        try {
            initializeClientIfNeeded();
            // You could add more specific checks here, like a ping request
            return true;
        } catch (Exception e) {
            logger.warning("Failed to connect to Elasticsearch: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get a client instance of the requested type
     * @param clientClass The type of client to return
     * @param <T> Type parameter for the client
     * @return The client instance
     * @throws UnsupportedOperationException if the requested client type is not supported
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getClient(@NotNull Class<T> clientClass) {
        if (clientClass.equals(ElasticsearchClient.class)) {
            initializeClientIfNeeded();
            return (T) elasticsearchClient;
        } else if (clientClass.equals(RestClient.class)) {
            initializeClientIfNeeded();
            return (T) restClient;
        } else if (clientClass.equals(RestClientTransport.class)) {
            initializeClientIfNeeded();
            return (T) transport;
        }

        throw new UnsupportedOperationException("Client type " + clientClass.getName() +
                " is not supported by Elasticsearch connection");
    }
}

