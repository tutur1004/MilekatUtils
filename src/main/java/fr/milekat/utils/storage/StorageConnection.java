package fr.milekat.utils.storage;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import fr.milekat.utils.storage.adapter.sql.connection.SQLDataBaseConnection;
import fr.milekat.utils.storage.exceptions.StorageLoadException;

import java.io.InputStream;

@SuppressWarnings("unused")
public interface StorageConnection extends AutoCloseable {

    StorageVendor getVendor();

    void close();

    boolean checkStoragesConnection() throws StorageLoadException;

    /**
     * Elasticsearch things
     */
    default ElasticsearchClient getEsClient() {
        throw new UnsupportedOperationException("This method is not supported for this storage vendor");
    }

    /**
     * SQL things
     */
    default SQLDataBaseConnection getSqlDataBaseConnection() {
        throw new UnsupportedOperationException("This method is not supported for this storage vendor");
    }

    default void loadSchema(InputStream schemaFile) throws StorageLoadException {
        throw new UnsupportedOperationException("This method is not supported for this storage vendor");
    }
}
