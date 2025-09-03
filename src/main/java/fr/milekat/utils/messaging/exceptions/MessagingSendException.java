package fr.milekat.utils.messaging.exceptions;

/**
 * Exception thrown when message sending operations fail.
 *
 * <p>This runtime exception is typically thrown during:
 * <ul>
 *   <li>Message publishing failures to messaging brokers</li>
 *   <li>JSON message format creation errors</li>
 *   <li>Network connectivity issues during send operations</li>
 *   <li>Channel or exchange access problems</li>
 * </ul>
 *
 * @author MileKat
 * @since 1.6
 */
public class MessagingSendException extends RuntimeException {

    /**
     * Creates a new MessagingSendException with the specified error message.
     *
     * @param message detailed description of the sending failure
     */
    public MessagingSendException(String message) {
        super(message);
    }
}