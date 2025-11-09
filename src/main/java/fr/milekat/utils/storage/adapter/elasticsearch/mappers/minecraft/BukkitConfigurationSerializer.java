package fr.milekat.utils.storage.adapter.elasticsearch.mappers.minecraft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class BukkitConfigurationSerializer extends StdSerializer<ConfigurationSerializable> {
    private final ObjectMapper mapper;

    public BukkitConfigurationSerializer(ObjectMapper mapper) {
        super(ConfigurationSerializable.class);
        this.mapper = mapper;
    }

    @Override
    public void serialize(@NotNull ConfigurationSerializable value,
                          @NotNull JsonGenerator gen,
                          SerializerProvider provider) throws IOException {
        // Create a temporary YamlConfiguration
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("data", value);

        // Convert the YamlConfiguration to a JsonNode
        String yamlString = yaml.saveToString();
        JsonNode node = mapper.readValue(yamlString, JsonNode.class);

        // Write the "data" node to the JsonGenerator
        gen.writeObject(node.get("data"));
    }
}