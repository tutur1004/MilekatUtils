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

public class BukkitConfigurationSerializer<T extends ConfigurationSerializable> extends StdSerializer<T> {
    private final ObjectMapper mapper;

    public BukkitConfigurationSerializer(Class<T> type, ObjectMapper mapper) {
        super(type);
        this.mapper = mapper;
    }

    @Override
    public void serialize(@NotNull T value, @NotNull JsonGenerator gen, SerializerProvider provider) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        String key = handledType().getSimpleName().toLowerCase();
        yaml.set(key, value);

        String yamlString = yaml.saveToString();
        JsonNode node = mapper.readValue(yamlString, JsonNode.class);

        gen.writeStartObject();
        gen.writeFieldName(key);
        gen.writeObject(node.get(key));
        gen.writeEndObject();
    }
}