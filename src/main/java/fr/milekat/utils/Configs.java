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

    public Configs(File fileConfig) {
        this.fileConfig = fileConfig;
    }

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

    public @NotNull String getString(@NotNull String node) {
        Object nodeValue = getValue(node);
        return nodeValue!=null ? nodeValue.toString() : "";
    }

    public @Nullable String getString(@NotNull String node, @Nullable String def) {
        Object nodeValue = getValue(node);
        return nodeValue!=null ? nodeValue.toString() : def;
    }

    public @NotNull Integer getInt(@NotNull String node) {
        try {
            return Integer.valueOf(getString(node));
        } catch (Exception exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public @NotNull Integer getInt(@NotNull String node, @NotNull Integer def) {
        try {
            return Integer.valueOf(getString(node), def);
        } catch (Exception ignored) {
            return def;
        }
    }

    public @Nullable Boolean getBoolean(@NotNull String node) {
        try {
            Object nodeValue = getValue(node);
            return (Boolean) nodeValue;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public @NotNull Boolean getBoolean(@NotNull String node, @NotNull Boolean def) {
        try {
            Object nodeValue = getValue(node);
            return nodeValue!=null ? (Boolean) nodeValue : def;
        } catch (ClassCastException ignored) {
            return def;
        }
    }

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

    public @NotNull List<String> getStringList(@NotNull String node) {
        return getList(node).stream().map(Object::toString).collect(Collectors.toList());
    }

    public @NotNull String getMessage(@NotNull String node) {
        return minecraftColorCodes(getString(node));
    }

    public @NotNull String getMessage(@NotNull String node, @NotNull String def) {
        return minecraftColorCodes(Objects.requireNonNull(getString(node, def)));
    }

    public @NotNull List<String> getMessages(@NotNull String node) {
        List<String> messages = new ArrayList<>();
        getStringList(node).forEach(line -> messages.add(minecraftColorCodes(line)));
        return messages;
    }

    /**
     * From net.md_5.bungee.api.ChatColor#translateAlternateColorCodes()
     */
    @Contract("_ -> new")
    private @NotNull String minecraftColorCodes(@NotNull String textToTranslate)
    {
        char[] b = textToTranslate.toCharArray();
        for ( int i = 0; i < b.length - 1; i++ )
        {
            String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
            if ( b[i] == '&' && ALL_CODES.indexOf( b[i + 1] ) > -1 )
            {
                char COLOR_CHAR = '§';
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase( b[i + 1] );
            }
        }
        return new String( b );
    }
}

