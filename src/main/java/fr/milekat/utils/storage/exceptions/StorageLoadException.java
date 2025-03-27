package fr.milekat.utils.storage.exceptions;

public class StorageLoadException extends Exception {
    /**
     * Constructs a new StorageLoadException with the specified detail message.
     *
     * @param errorMessage the detail message
     */
    public StorageLoadException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new StorageLoadException with the specified detail message and cause.
     *
     * @param errorMessage the detail message
     * @param cause the cause of the exception
     */
    public StorageLoadException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}

