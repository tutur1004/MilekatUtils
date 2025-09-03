package fr.milekat.utils.messaging;

/**
 * Enumeration of supported messaging vendors with their adapter and driver information.
 *
 * <p>Defines the mapping between messaging vendor names, their adapter classes,
 * and required driver dependencies for dynamic loading and classpath detection.
 *
 * @author MileKat
 * @since 1.6
 */
public enum MessagingVendor {

    /** RabbitMQ messaging vendor */
    RABBITMQ("rabbitmq", "RabbitMQConnection",
            "com.rabbitmq.client.Connection"),

    /** Redis messaging vendor */
    REDIS("redis", "RedisConnection",
            "redis.clients.jedis.Jedis"),
    ;

    /** The vendor adapter identifier */
    private final String messagingAdapter;

    /** Full class name of the vendor's connection adapter */
    private final String adapterConnectionClass;

    /** Driver class used for classpath detection */
    private final String driverClass;

    /**
     * Creates a messaging vendor definition.
     *
     * @param messagingAdapter the vendor identifier
     * @param adapterConnectionClass the connection class name (without package)
     * @param driverClass the driver class for classpath detection
     */
    MessagingVendor(String messagingAdapter, String adapterConnectionClass, String driverClass) {
        this.messagingAdapter = messagingAdapter;
        this.adapterConnectionClass = "fr.milekat.utils.messaging.adapter." +
                messagingAdapter + "." + adapterConnectionClass;
        this.driverClass = driverClass;

    }

    /**
     * Gets the vendor adapter identifier.
     *
     * @return the messaging adapter name
     */
    public String getMessagingAdapter() {
        return messagingAdapter;
    }

    /**
     * Gets the full adapter connection class name.
     *
     * @return the complete class path for the adapter connection
     */
    public String getAdapterConnectionClass() {
        return adapterConnectionClass;
    }

    /**
     * Gets the driver class name for classpath detection.
     *
     * @return the driver class used to verify vendor availability
     */
    public String getDriverClass() {
        return driverClass;
    }
}