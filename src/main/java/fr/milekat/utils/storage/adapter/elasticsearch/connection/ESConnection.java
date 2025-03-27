package fr.milekat.utils.storage.adapter.elasticsearch.connection;

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

@SuppressWarnings("unused")
public class ESConnection implements StorageConnection, AutoCloseable {
    private final MileLogger logger;
    private RestClient restClient;
    private RestClientTransport transport;
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
        try {
            if (transport != null) {
                transport.close();
            }
            if (restClient != null) {
                restClient.close();
            }
            // Reset references
            this.transport = null;
            this.restClient = null;
        } catch (IOException exception) {
            logger.warning("Error while closing Elasticsearch connection: " + exception.getMessage());
        }
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
        this.restClient = getRestClient();
        this.transport = new RestClientTransport(restClient, mapper);
        return new ElasticsearchClient(transport);
    }
}

