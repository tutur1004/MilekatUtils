package fr.milekat.utils.storage.adapter.elasticsearch.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.milekat.utils.storage.adapter.elasticsearch.mappers.minecraft.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to get a new ObjectMapper with my custom mappers
 * @implNote This method requires
 * <a href="https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind">
 *     jackson-databind</a> and
 * <a href="https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml">
 *     jackson-dataformat-yaml</a> to be included in the classpath
 */
@SuppressWarnings("unused")
public class MinecraftMappers {
    /**
     * Get a new ObjectMapper for Minecraft
     *
     * @implSpec This method requires a {@link org.bukkit.Bukkit} server to be running
     * @return a new {@link ObjectMapper} with my custom mappers
     */
    @Contract(" -> new")
    public static @NotNull ObjectMapper getMinecraftMapper() {
        //  Create a new ObjectMapper with YAMLFactory
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule module = new SimpleModule();
        //  ItemStack
        module.addSerializer(ItemStack.class, new ItemStackSerializer(mapper));
        module.addDeserializer(ItemStack.class, new ItemStackDeserializer(mapper));
        // Inventory
        module.addSerializer(Inventory.class, new InventorySerializer(mapper));
        module.addDeserializer(Inventory.class, new InventoryDeserializer(mapper));
        //  Location
        module.addSerializer(Location.class, new LocationSerializer(mapper));
        module.addDeserializer(Location.class, new LocationDeserializer(mapper));
        //  Block
        module.addSerializer(Block.class, new BlockSerializer(mapper));
        module.addDeserializer(Block.class, new BlockDeserializer(mapper));
        //  Register modules to mapper
        mapper.registerModule(module);
        //  Return the mapper
        return mapper;
    }
}
