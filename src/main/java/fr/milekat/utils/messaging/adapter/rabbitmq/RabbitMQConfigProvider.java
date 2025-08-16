package fr.milekat.utils.messaging.adapter.rabbitmq;

import fr.milekat.utils.Configs;
import org.jetbrains.annotations.NotNull;

public class RabbitMQConfigProvider {
    private final String separator;
    private final String prefix;
    private final String proxyPrefix;
    private final String serverIdentifier;

    public RabbitMQConfigProvider(@NotNull Configs configs) {
        this.separator = ".";
        this.prefix = configs.getString("messaging.prefix", "minecraft");
        this.proxyPrefix = configs.getString("messaging.proxy_prefix", "proxy");
        this.serverIdentifier = configs.getString("messaging.server_identifier", "unknown");
    }

    public String getSeparator() {
        return separator;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getProxyPrefix() {
        return proxyPrefix;
    }

    public String getServerIdentifier() {
        return serverIdentifier;
    }

    public String getRabbitExchangeType() {
        return "x-rtopic";
    }

    public String getRabbitExchange() {
        return prefix + separator + getRabbitExchangeType() + separator + "exchange";
    }

    public String getRabbitQueue() {
        return prefix + separator + "queue" + separator + serverIdentifier;
    }

    public String getRabbitRoutingKey() {
        return prefix + separator + serverIdentifier;
    }

    public String getRabbitToAllProxy() {
        return prefix + separator + proxyPrefix + separator + "#";
    }
}
