package fr.milekat.utils.messaging;

import fr.milekat.utils.Configs;
import fr.milekat.utils.MileLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MessagingLoader {
    public static MileLogger messagingLogger = new MileLogger("MessagingLoader");
    private final MessagingConnection loadedMessaging;

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

    public MessagingConnection getLoadedMessaging() {
        return loadedMessaging;
    }

    public static MileLogger getMessagingLogger() {
        return messagingLogger;
    }
}
