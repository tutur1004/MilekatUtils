package fr.milekat.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class McNamesTest {
    @Test
    void testGetUuid() throws IOException {
        String name = "Notch";
        String expectedUuid = "069a79f4-44e9-4726-a5be-fca90e38aaf5";
        String actualUuid = McNames.getUuid(name);
        Assertions.assertEquals(expectedUuid, actualUuid);
    }

    @Test
    void testGetName() {
        String uuid = "069a79f4-44e9-4726-a5be-fca90e38aaf5";
        String expectedName = "Notch";
        String actualName = McNames.getName(uuid);
        Assertions.assertEquals(expectedName, actualName);
    }
}
