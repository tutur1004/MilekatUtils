package fr.milekat.utils.storage;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import fr.milekat.utils.storage.adapter.sql.connection.SQLDataBaseConnection;
import fr.milekat.utils.storage.exceptions.StorageLoadException;

import java.io.InputStream;

@SuppressWarnings("unused")
public interface StorageConnection extends AutoCloseable {

    StorageVendor getVendor();

    void close();

    boolean checkStoragesConnection() throws StorageLoadException;

    //  TODO: Make somethings to prevent require ElasticsearchClient or SQLDataBaseConnection in classpath

    /**
     * Elasticsearch things
     * @return ElasticsearchClient
     */
    default ElasticsearchClient getEsClient() {
        return getEsClient(new JacksonJsonpMapper());
    }
    default ElasticsearchClient getEsClient(JacksonJsonpMapper mapper) {
        throw new UnsupportedOperationException("This method is not supported for this storage vendor");
    }

    /**
     * SQL things
     * @return SQLDataBaseConnection
     */
    default SQLDataBaseConnection getSqlDataBaseConnection() {
        throw new UnsupportedOperationException("This method is not supported for this storage vendor");
    }

    default void loadSchema(InputStream schemaFile) throws StorageLoadException {
        throw new UnsupportedOperationException("This method is not supported for this storage vendor");
    }
}
