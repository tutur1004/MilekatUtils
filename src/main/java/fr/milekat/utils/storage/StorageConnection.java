package fr.milekat.utils.storage;

import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

@SuppressWarnings("unused")
public interface StorageConnection extends AutoCloseable {

    boolean checkStoragesConnection() throws StorageLoadException;

    void close();

    StorageVendor getVendor();

    default void loadSchema(InputStream schemaFile) throws StorageLoadException {
        throw new UnsupportedOperationException("This method is not supported for this storage vendor");
    }

    default @Nullable String getPrefix() {
        return null;
    }
}
