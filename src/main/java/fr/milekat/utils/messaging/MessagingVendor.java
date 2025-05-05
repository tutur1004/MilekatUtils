package fr.milekat.utils.messaging;

public enum MessagingVendor {
    RABBITMQ("rabbitmq", "RabbitMQConnection",
            "com.rabbitmq.client.Connection"),
    REDIS("redis", "RedisConnection",
            "redis.clients.jedis.Jedis"),
    ;

    private final String messagingAdapter;
    private final String adapterConnectionClass;
    private final String driverClass;

    MessagingVendor(String messagingAdapter, String adapterConnectionClass, String driverClass) {
        this.messagingAdapter = messagingAdapter;
        this.adapterConnectionClass = "fr.milekat.utils.messaging.adapter." +
                messagingAdapter + ".connection." + adapterConnectionClass;
        this.driverClass = driverClass;

    }

    public String getMessagingAdapter() {
        return messagingAdapter;
    }

    public String getAdapterConnectionClass() {
        return adapterConnectionClass;
    }

    public String getDriverClass() {
        return driverClass;
    }
}
