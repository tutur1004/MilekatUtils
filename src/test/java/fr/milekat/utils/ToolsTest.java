package fr.milekat.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ToolsTest {

    @Test
    public void testRemLastChar() {
        String input = "Hello World!";
        String expectedOutput = "Hello World";
        String actualOutput = Tools.remLastChar(input);
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testIsAlphaNumericExtended() {
        String input1 = "abc123";
        String input2 = "abc!@#";
        Assertions.assertTrue(Tools.isAlphaNumericExtended(input1));
        Assertions.assertFalse(Tools.isAlphaNumericExtended(input2));
    }

    @Test
    public void testGetString() {
        Assertions.assertNotNull(Tools.getString(1));
        Assertions.assertEquals(":zero:", Tools.getString(0));
    }

    @Test
    public void testGetInt() {
        Assertions.assertEquals(0, Tools.getInt(":zero:"));
    }

    @Test
    public void testGetRandomString() {
        int length = 10;
        String randomString = Tools.getRandomString(length);
        Assertions.assertEquals(length, randomString.length());
    }
}

