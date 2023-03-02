package fr.milekat.utils;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Configs {
    private final File fileConfig;

    public Configs(File fileConfig) {
        this.fileConfig = fileConfig;
    }

    private List<String> getKeys(@NotNull String node) {
        if (node.contains(".")) {
            return Arrays.asList(node.split("\\."));
        } else {
            return Collections.singletonList(node);
        }
    }

    private @Nullable ConfigurationNode getValue(@NotNull String node) {
        if (fileConfig==null) return null;
        try {
            ConfigurationNode valueNode = YAMLConfigurationLoader.builder().setFile(fileConfig).build().load();
            for (String key : getKeys(node)) {
                valueNode = valueNode.getNode(key);
            }
            return valueNode;
        } catch (IOException ignored) {
            return null;
        }
    }

    public @NotNull String getString(@NotNull String node) {
        ConfigurationNode nodeValue = getValue(node);
        return nodeValue!=null ? nodeValue.getString("") : "";
    }

    public @Nullable String getString(@NotNull String node, @Nullable String def) {
        ConfigurationNode nodeValue = getValue(node);
        return nodeValue!=null ? nodeValue.getString(def) : def;
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
        return Objects.requireNonNull(getValue(node)).getBoolean();
    }

    public @NotNull Boolean getBoolean(@NotNull String node, @NotNull Boolean def) {
        try {
            return Objects.requireNonNull(getValue(node)).getBoolean();
        } catch (Exception ignored) {
            return def;
        }
    }

    public @NotNull List<?> getList(@NotNull String node) {
        List<?> list = new ArrayList<>();
        Object obj = Objects.requireNonNull(getValue(node)).getValue();
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

