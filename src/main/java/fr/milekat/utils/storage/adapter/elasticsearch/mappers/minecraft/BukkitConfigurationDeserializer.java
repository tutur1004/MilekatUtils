package fr.milekat.utils.storage.adapter.elasticsearch.mappers.minecraft;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class BukkitConfigurationDeserializer extends StdDeserializer<ConfigurationSerializable> {
    private final ObjectMapper mapper;

    public BukkitConfigurationDeserializer(ObjectMapper mapper) {
        super(ConfigurationSerializable.class);
        this.mapper = mapper;
    }

    @Nullable
    @Override
    public ConfigurationSerializable deserialize(JsonParser p, DeserializationContext context) throws IOException {
        try {
            JsonNode node = mapper.readTree(p);
            if (node == null || node.isEmpty()) {
                return null;
            }

            // Create a temporary YamlConfiguration
            YamlConfiguration yaml = new YamlConfiguration();
            String yamlString = new YAMLMapper().writeValueAsString(node);
            yaml.loadFromString("data:" + yamlString);

            // Retrieve the ConfigurationSerializable object
            Object result = yaml.get("data");

            if (result instanceof ConfigurationSerializable) {
                return (ConfigurationSerializable) result;
            }

            return null;
        } catch (InvalidConfigurationException e) {
            throw new IOException("Failed to deserialize ConfigurationSerializable", e);
        }
    }
}