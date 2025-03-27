package fr.milekat.utils.storage;

import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

@SuppressWarnings("unused")
public interface StorageConnection extends AutoCloseable {

    StorageVendor getVendor();

    void close();

    boolean checkStoragesConnection() throws StorageLoadException;

    /**
     * Returns a specific client connection by type
     * @param <T> Type of client to return
     * @param clientClass Class representing the client type
     * @return The client instance if supported by this storage vendor
     * @throws UnsupportedOperationException if client type is not supported
     */
    <T> T getClient(@NotNull Class<T> clientClass);

    default void loadSchema(InputStream schemaFile) throws StorageLoadException {
        throw new UnsupportedOperationException("This method is not supported for this storage vendor");
    }

    default @Nullable String getPrefix() {
        return null;
    }
}
