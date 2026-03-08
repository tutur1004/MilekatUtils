package fr.milekat.utils.storage.adapter.elasticsearch.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.milekat.utils.storage.adapter.elasticsearch.mappers.minecraft.*;
import org.bukkit.*;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Utility class to get a new ObjectMapper with custom mappers
 * @apiNote This method requires
 * <a href="https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind">
 *     jackson-databind</a> and
 * <a href="https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml">
 *     jackson-dataformat-yaml</a> to be included in the classpath
 */
@SuppressWarnings("unused")
public class MinecraftMappers {
    private static final List<Class<? extends ConfigurationSerializable>> BUKKIT_TYPES = List.of(
            AttributeModifier.class,
            BlockVector.class,
            BoundingBox.class,
            Color.class,
            FireworkEffect.class,
            ItemStack.class,
            Location.class,
            Pattern.class,
            PotionEffect.class,
            Vector.class
    );

    /**
     * Get a new ObjectMapper for Minecraft
     *
     * @apiNote This method requires a {@link Bukkit} server to be running
     * @return a new {@link ObjectMapper} with custom mappers
     */
    @Contract(" -> new")
    public static @NotNull ObjectMapper getMapper() {
        // Create a new ObjectMapper with YAMLFactory
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule module = new SimpleModule();

        // Inventory
        module.addSerializer(Inventory.class, new InventorySerializer(mapper));
        module.addDeserializer(Inventory.class, new InventoryDeserializer(mapper));
        //  Block
        module.addSerializer(Block.class, new BlockSerializer(mapper));
        module.addDeserializer(Block.class, new BlockDeserializer(mapper));

        // Register generic serializer/deserializer for ConfigurationSerializable
        // This automatically handles: ItemStack, Location, etc.
        BUKKIT_TYPES.forEach(type -> registerBukkitType(module, mapper, type));

        // Register the module to the mapper
        mapper.registerModule(module);

        // Return the mapper
        return mapper;
    }

    private static <T extends ConfigurationSerializable> void registerBukkitType(@NotNull SimpleModule module,
                                                                                 @NotNull ObjectMapper mapper,
                                                                                 Class<T> type) {
        module.addSerializer(type, new BukkitConfigurationSerializer<>(type, mapper));
        module.addDeserializer(type, new BukkitConfigurationDeserializer<>(type, mapper));
    }
}