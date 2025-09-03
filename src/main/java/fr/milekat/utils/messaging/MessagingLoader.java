package fr.milekat.utils.messaging;

import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Factory class for loading and initializing messaging connections based on configuration.
 *
 * <p>Selects the appropriate messaging adapter (RabbitMQ, Redis, etc.) based on the
 * "messaging.type" configuration property and ensures the connection is ready for use.
 *
 * @author MileKat
 * @since 1.6
 */
@SuppressWarnings("unused")
public class MessagingLoader {

    /** Shared logger instance for messaging operations */
    public static MileLogger messagingLogger = new MileLogger("MessagingLoader");

    /** The loaded messaging connection instance */
    private final MessagingConnection loadedMessaging;

    /**
     * Creates a new messaging loader and initializes the specified messaging adapter.
     *
     * <p>Reads the messaging type from configuration, loads available adapters,
     * and selects the appropriate one. Validates that the connection is ready before completion.
     *
     * @param config configuration object containing messaging.type setting
     * @param logger logger instance for debugging and monitoring
     * @throws IllegalArgumentException if messaging type is unsupported or connection fails
     */
    public MessagingLoader(@NotNull Configs config, @NotNull MileLogger logger) {
        messagingLogger = logger;
        String messagingType = config.getString("messaging.type");
        messagingLogger.debug("Loading messaging type: " + messagingType);

        Map<String, MessagingConnection> messagingAdapters = MessagingAdapterLoader.loadAdapters(config, logger);

        switch (messagingType.toLowerCase()) {
            case "rabbitmq":
                loadedMessaging = messagingAdapters.get("rabbitmq");
                break;
            case "redis":
                loadedMessaging = messagingAdapters.get("redis");
                break;
            default:
                throw new IllegalArgumentException("Unsupported messaging type");
        }

        if (loadedMessaging.connectionReady()) {
            messagingLogger.debug("Messaging loaded");
        } else {
            throw new IllegalArgumentException("Messaging is not loaded properly");
        }
    }

    /**
     * Gets the initialized messaging connection instance.
     *
     * @return the loaded and ready messaging connection
     */
    public MessagingConnection getLoadedMessaging() {
        return loadedMessaging;
    }

    /**
     * Gets the shared messaging logger instance.
     *
     * @return the messaging logger
     */
    public static MileLogger getMessagingLogger() {
        return messagingLogger;
    }
}