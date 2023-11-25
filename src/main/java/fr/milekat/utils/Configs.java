package fr.milekat.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Configs {
    private final File fileConfig;

    /**
     * Constructs a new Configs instance with the specified file.
     *
     * @param fileConfig The configuration file.
     */
    public Configs(File fileConfig) {
        this.fileConfig = fileConfig;
    }

    /**
     * Retrieves the value of a node in the configuration.
     *
     * @param path   The path to the node.
     * @param config The configuration map.
     * @return The value of the node, or null if the node is not found.
     */
    public static @Nullable Object getNodeValue(@NotNull String path, Map<String, Object> config) {
        String[] keys = path.split("\\.");
        Object node = config;
        for (String key : keys) {
            if (node instanceof Map) {
                node = ((Map<?, ?>) node).get(key);
            } else {
                return null;
            }
        }
        return node;
    }

    private @Nullable Object getValue(@NotNull String node) {
        if (fileConfig==null) return null;
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> valueNode = yaml.load(Files.newInputStream(fileConfig.toPath()));
            return getNodeValue(node, valueNode);
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Retrieves the string value of a node in the configuration.
     *
     * @param node The path to the node.
     * @return The string value of the node, or an empty string if the node is not found.
     */
    public @NotNull String getString(@NotNull String node) {
        Object nodeValue = getValue(node);
        return nodeValue!=null ? nodeValue.toString() : "";
    }

    /**
     * Retrieves the string value of a node in the configuration, with a default value.
     *
     * @param node The path to the node.
     * @param def  The default value.
     * @return The string value of the node, or the default value if the node is not found.
     */
    public @NotNull String getString(@NotNull String node, @NotNull String def) {
        Object nodeValue = getValue(node);
        return nodeValue!=null ? nodeValue.toString() : def;
    }

    /**
     * Retrieves the integer value of a node in the configuration.
     *
     * @param node The path to the node.
     * @return The integer value of the node, or 0 if the node is not found or not a valid integer.
     */
    public @NotNull Integer getInt(@NotNull String node) {
        try {
            return Integer.valueOf(getString(node));
        } catch (Exception exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    /**
     * Retrieves the integer value of a node in the configuration, with a default value.
     *
     * @param node The path to the node.
     * @param def  The default value.
     * @return The integer value of the node, or the default value if the node is not found or not a valid integer.
     */
    public @NotNull Integer getInt(@NotNull String node, @NotNull Integer def) {
        try {
            return Integer.valueOf(getString(node));
        } catch (Exception ignored) {
            return def;
        }
    }

    /**
     * Retrieves the long value of a node in the configuration.
     *
     * @param node The path to the node.
     * @return The long value of the node, or 0 if the node is not found or not a valid long.
     */
    public @NotNull Long getLong(@NotNull String node) {
        try {
            return Long.valueOf(getString(node));
        } catch (Exception exception) {
            exception.printStackTrace();
            return 0L;
        }
    }

    /**
     * Retrieves the long value of a node in the configuration, with a default value.
     *
     * @param node The path to the node.
     * @param def  The default value.
     * @return The long value of the node, or the default value if the node is not found or not a valid long.
     */
    public @NotNull Long getLong(@NotNull String node, @NotNull Long def) {
        try {
            return Long.valueOf(getString(node));
        } catch (Exception ignored) {
            return def;
        }
    }

    /**
     * Retrieves the double value of a node in the configuration.
     *
     * @param node The path to the node.
     * @return The double value of the node, or 0 if the node is not found or not a valid double.
     */
    public @NotNull Double getDouble(@NotNull String node) {
        try {
            return Double.valueOf(getString(node));
        } catch (Exception exception) {
            exception.printStackTrace();
            return 0D;
        }
    }

    /**
     * Retrieves the double value of a node in the configuration, with a default value.
     *
     * @param node The path to the node.
     * @param def  The default value.
     * @return The double value of the node, or the default value if the node is not found or not a valid double.
     */
    public @NotNull Double getDouble(@NotNull String node, @NotNull Double def) {
        try {
            return Double.valueOf(getString(node));
        } catch (Exception ignored) {
            return def;
        }
    }

    /**
     * Retrieves the boolean value of a node in the configuration.
     *
     * @param node The path to the node.
     * @return The boolean value of the node, or false if the node is not found or not a valid boolean.
     */
    public @Nullable Boolean getBoolean(@NotNull String node) {
        try {
            Object nodeValue = getValue(node);
            return (Boolean) nodeValue;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the boolean value of a node in the configuration, with a default value.
     *
     * @param node The path to the node.
     * @param def  The default value.
     * @return The boolean value of the node, or the default value if the node is not found or not a valid boolean.
     */
    public @NotNull Boolean getBoolean(@NotNull String node, @NotNull Boolean def) {
        try {
            Object nodeValue = getValue(node);
            return nodeValue != null ? (Boolean) nodeValue : def;
        } catch (ClassCastException ignored) {
            return def;
        }
    }

    /**
     * Retrieves a list value of a node in the configuration.
     *
     * @param node The path to the node.
     * @return The list value of the node, or an empty list if the node is not found or not a valid list.
     */
    public @NotNull List<?> getList(@NotNull String node) {
        List<?> list = new ArrayList<>();
        Object obj = getValue(node);
        if (obj != null && obj.getClass().isArray()) {
            list = Arrays.asList((Object[]) obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>) obj);
        }
        return list;
    }

    /**
     * Retrieves a list of strings from a node in the configuration.
     *
     * @param node The path to the node.
     * @return The list of strings, or an empty list if the node is not found or not a valid list.
     */
    public @NotNull List<String> getStringList(@NotNull String node) {
        return getList(node).stream().map(Object::toString).collect(Collectors.toList());
    }

    /**
     * Retrieves a minecraft formatted message from a node in the configuration, translating color codes.
     *
     * @param node The path to the node.
     * @return The minecraft formatted message, with color codes translated.
     */
    public @NotNull String getMessage(@NotNull String node) {
        return minecraftColorCodes(getString(node));
    }

    /**
     * Retrieves a minecraft formatted message from a node in the configuration,
     * with a default value, translating color codes.
     *
     * @param node The path to the node.
     * @param def  The default value.
     * @return The minecraft formatted message, with color codes translated,
     * or the default value if the node is not found.
     */
    public @NotNull String getMessage(@NotNull String node, @NotNull String def) {
        return minecraftColorCodes(getString(node, def));
    }

    /**
     * Retrieves a list of minecraft formatted messages from a node in the configuration, translating color codes.
     *
     * @param node The path to the node.
     * @return The list of minecraft formatted messages, with color codes translated.
     */
    public @NotNull List<String> getMessages(@NotNull String node) {
        List<String> messages = new ArrayList<>();
        getStringList(node).forEach(line -> messages.add(minecraftColorCodes(line)));
        return messages;
    }

    /**
     * Translates Minecraft color codes in a text string.
     *
     * @param textToTranslate The text to translate.
     * @return The translated text with Minecraft color codes.
     * @author From net.md_5.bungee.api.ChatColor#translateAlternateColorCodes()
     */
    @Contract("_ -> new")
    private @NotNull String minecraftColorCodes(@NotNull String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
            if (b[i] == '&' && ALL_CODES.indexOf(b[i + 1]) > -1) {
                char COLOR_CHAR = (char) 167;
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
