package fr.milekat.utils.messaging.exceptions;

/**
 * Exception thrown when messaging connection or consumer initialization fails.
 *
 * <p>This runtime exception is typically thrown during:
 * <ul>
 *   <li>Connection establishment failures to messaging brokers</li>
 *   <li>Message processor registration errors</li>
 *   <li>Channel or queue creation issues</li>
 *   <li>Authentication or authorization problems</li>
 * </ul>
 *
 * @author MileKat
 * @since 1.6
 */
public class MessagingLoadException extends RuntimeException {

    /**
     * Creates a new MessagingLoadException with the specified error message.
     *
     * @param message detailed description of the loading failure
     */
    public MessagingLoadException(String message) {
        super(message);
    }
}
