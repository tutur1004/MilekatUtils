package fr.milekat.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigsTest {
    private Configs configs;

    @BeforeEach
    public void setUp() {
        Path path = Paths.get("src", "test", "resources", "config.yml");
        File file = path.toFile();
        configs = new Configs(file);
    }

    @Test
    public void testGetString() {
        String value = configs.getString("test.string");
        Assertions.assertEquals("hello world", value);
    }

    @Test
    public void testGetStringWithDefault() {
        String defaultValue = "default value";
        String value = configs.getString("invalid.node", defaultValue);
        Assertions.assertEquals(defaultValue, value);
    }

    @Test
    public void testGetInt() {
        int value = configs.getInt("test.int");
        Assertions.assertEquals(123, value);
    }

    @Test
    public void testGetIntWithDefault() {
        int defaultValue = 456;
        int value = configs.getInt("invalid.node", defaultValue);
        Assertions.assertEquals(defaultValue, value);
    }

    @Test
    public void testGetBoolean() {
        boolean value = Boolean.TRUE.equals(configs.getBoolean("test.boolean"));
        Assertions.assertTrue(value);
    }

    @Test
    public void testGetBooleanWithDefault() {
        boolean defaultValue = false;
        boolean value = configs.getBoolean("invalid.node", defaultValue);
        Assertions.assertEquals(defaultValue, value);
    }

    @Test
    public void testGetStringList() {
        List<String> value = configs.getStringList("test.list");
        Assertions.assertEquals(Arrays.asList("foo", "bar", "baz"), value);
    }

    @Test
    public void testGetMessage() {
        String value = configs.getMessage("test.message");
        Assertions.assertEquals("\u00A7aHello \u00A7bworld\u00A7r", value);
    }

    @Test
    public void testGetMessageWithDefault() {
        String defaultValue = "\u00A7cdefault\u00A7r";
        String value = configs.getMessage("invalid.node", defaultValue);
        Assertions.assertEquals(defaultValue, value);
    }

    @Test
    public void testGetMessages() {
        List<String> value = configs.getMessages("test.messages");
        Assertions.assertEquals(Arrays.asList("\u00A7aHello \u00A7bworld\u00A7r", "\u00A7cError: \u00A7eSomething went wrong!\u00A7r"), value);
    }

    @Test
    public void testGetNodeValue() {
        Map<String, Object> config = new HashMap<>();
        HashMap<String, String> object = new HashMap<>();
        object.put("bar", "baz");
        config.put("foo", object);
        Object value = Configs.getNodeValue("foo.bar", config);
        Assertions.assertEquals("baz", value);
    }
}

