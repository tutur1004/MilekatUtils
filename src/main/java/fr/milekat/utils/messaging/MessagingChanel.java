package fr.milekat.utils.messaging;

/**
 * Immutable configuration holder for messaging channel properties.
 *
 * <p>Stores channel name and type information used by messaging
 * adapters for broker configuration and message routing.
 *
 * @author MileKat
 * @since 1.6
 */
public class MessagingChanel {

    /** The channel name used for message routing (For RabbitMQ, this is the exchange name) */
    private final String NAME;

    /** The channel type (e.g., "direct", "topic", "fanout", "x-rtopic") */
    private final String TYPE;

    /**
     * Creates a new messaging channel configuration.
     *
     * @param name the channel name
     * @param type the channel type
     */
    public MessagingChanel(String name, String type) {
        this.NAME = name;
        this.TYPE = type;
    }

    /**
     * Gets the channel name.
     *
     * @return the channel name
     */
    public String getName() {
        return NAME;
    }

    /**
     * Gets the channel type.
     *
     * @return the channel type
     */
    public String getType() {
        return TYPE;
    }
}