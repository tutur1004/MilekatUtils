package fr.milekat.utils.storage.adapter.elasticsearch.mappers.minecraft;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.IOException;

public class BukkitConfigurationDeserializer<T extends ConfigurationSerializable> extends StdDeserializer<T> {
    private final ObjectMapper mapper;

    public BukkitConfigurationDeserializer(Class<T> type, ObjectMapper mapper) {
        super(type);
        this.mapper = mapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser p, DeserializationContext context) throws IOException {
        try {
            JsonNode node = mapper.readTree(p);
            if (node == null || node.isEmpty()) return null;

            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(new YAMLMapper().writeValueAsString(node));

            for (String key : yaml.getKeys(false)) {
                Object result = yaml.get(key);
                if (handledType().isInstance(result)) {
                    return (T) result;
                }
            }
            return null;
        } catch (InvalidConfigurationException e) {
            throw new IOException("Failed to deserialize", e);
        }
    }
}