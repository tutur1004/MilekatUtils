package fr.milekat.utils.storage.exceptions;

public class StorageExecuteException extends Exception {
    private final String message;

    /**
     * Issue during a storage execution
     */
    public StorageExecuteException(Throwable exception, String message) {
        super(exception);
        this.message = message;
    }

    /**
     * Get error message (If exist)
     */
    public String getMessage() {
        return message;
    }
}
