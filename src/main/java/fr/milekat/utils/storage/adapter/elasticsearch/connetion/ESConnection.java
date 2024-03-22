package fr.milekat.utils.storage.adapter.elasticsearch.connetion;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import fr.milekat.utils.storage.StorageConnection;
import fr.milekat.utils.storage.StorageLoader;
import fr.milekat.utils.storage.StorageVendor;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

public class ESConnection implements StorageConnection {
    private final RestClient restClient;
    private final RestClientTransport transport;

    public ESConnection(RestClient restClient) {
        this.restClient = restClient;
        this.transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Override
    public boolean checkStoragesConnection() {
        return true;
    }

    @Override
    public ElasticsearchClient getEsClient() {
        return new ElasticsearchClient(transport);
    }

    @Override
    public StorageVendor getVendor() {
        return StorageVendor.ELASTICSEARCH;
    }

    @Override
    public void close() {
        try {
            transport.close();
            restClient.close();
        } catch (IOException exception) {
            StorageLoader.getStorageLogger().warning("Error while closing connection.");
        }
    }
}
