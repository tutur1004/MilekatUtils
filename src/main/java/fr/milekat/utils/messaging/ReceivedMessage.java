package fr.milekat.utils.messaging;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Represents a message received from a messaging system.
 * Provides methods to access the message content, routing key,
 * and to acknowledge or reject the message.
 */
@SuppressWarnings("unused")
public interface ReceivedMessage {
    /**
     * Get the routing key of the message
     * @return the routing key
     */
    String getRoutingKey();

    /**
     * Get the sender's routing key (callback routing key)
     * <p>
     * This is used to send a reply or acknowledgment back to the sender.
     * </p>
     *
     * @return the sender's routing key, or null if not set
     */
    @Nullable String getCallbackRoutingKey();

    /**
     * Get the message content
     * @return the message content
     */
    String getMessage();

    /**
     * Acknowledge the message (mark as processed successfully)
     *
     * @throws IOException if an error occurs during acknowledgment
     */
    void ack() throws IOException;

    /**
     * Reject the message (mark as failed, won't be re-queued)
     *
     * @throws IOException if an error occurs during rejection
     */
    void reject() throws IOException;

    /**
     * Check if the message has been acknowledged
     * @return true if the message has been acknowledged
     */
    boolean isAcknowledged();
}
