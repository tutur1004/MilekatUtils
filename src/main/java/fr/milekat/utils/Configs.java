package fr.milekat.utils;

import net.md_5.bungee.api.ChatColor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Configs {
    private final FileConfiguration bukkitConfiguration;
    private final File fileConfig;

    public Configs(FileConfiguration bukkitConfiguration) {
        this.bukkitConfiguration = bukkitConfiguration;
        this.fileConfig = null;
    }

    public Configs(File fileConfig) {
        this.bukkitConfiguration = null;
        this.fileConfig = fileConfig;
    }

    @Contract(pure = true)
    private @NotNull Boolean isBukkit() {
        return bukkitConfiguration!=null && fileConfig==null;
    }

    private List<String> getKeys(@NotNull String node) {
        if (node.contains(".")) {
            return Arrays.asList(node.split("\\."));
        } else {
            return Collections.singletonList(node);
        }
    }

    private @Nullable ConfigurationNode getValue(String node) {
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

    public @NotNull String getString(String node) {
        if (isBukkit()) {
            return bukkitConfiguration.getString(node, "");
        } else {
            ConfigurationNode nodeValue = getValue(node);
            return nodeValue!=null ? nodeValue.getString("") : "";
        }
    }

    public @NotNull String getString(String node, String def) {
        if (isBukkit()) {
            return bukkitConfiguration.getString(node, def);
        } else {
            ConfigurationNode nodeValue = getValue(node);
            return nodeValue!=null ? nodeValue.getString(def) : def;
        }
    }

    public @NotNull Integer getInt(String node) {
        try {
            if (isBukkit()) {
                return bukkitConfiguration.getInt(node);
            } else {
                return Integer.valueOf(getString(node));
            }
        } catch (Exception ignored) {
            return 0;
        }
    }

    public @NotNull Integer getInt(String node, Integer def) {
        try {
            if (isBukkit()) {
                return bukkitConfiguration.getInt(node, def);
            } else {
                return Integer.valueOf(getString(node), def);
            }
        } catch (Exception ignored) {
            return def;
        }
    }

    public @Nullable Boolean getBoolean(String node) {
        if (isBukkit()) {
            return bukkitConfiguration.getBoolean(node);
        } else {
            return Objects.requireNonNull(getValue(node)).getBoolean();
        }
    }

    public @NotNull Boolean getBoolean(String node, @NotNull Boolean def) {
        try {
            if (isBukkit()) {
                return bukkitConfiguration.getBoolean(node);
            } else {
                return Objects.requireNonNull(getValue(node)).getBoolean();
            }
        } catch (Exception ignored) {
            return def;
        }
    }

    public @NotNull List<?> getList(String node) {
        if (isBukkit()) {
            return bukkitConfiguration.getList(node, new ArrayList<>());
        } else {
            List<?> list = new ArrayList<>();
            Object obj = Objects.requireNonNull(getValue(node)).getValue();
            if (obj != null && obj.getClass().isArray()) {
                list = Arrays.asList((Object[]) obj);
            } else if (obj instanceof Collection) {
                list = new ArrayList<>((Collection<?>) obj);
            }
            return list;
        }
    }

    public @NotNull List<String> getStringList(String node) {
        if (isBukkit()) {
            return bukkitConfiguration.getStringList(node);
        } else {
            return getList(node).stream().map(Object::toString).collect(Collectors.toList());
        }
    }

    public @NotNull String getMessage(String node) {
        return ChatColor.translateAlternateColorCodes('&', getString(node));
    }

    public @NotNull List<String> getMessages(String node) {
        List<String> messages = new ArrayList<>();
        getStringList(node).forEach(line -> messages.add(ChatColor.translateAlternateColorCodes('&', line)));
        return messages;
    }
}

