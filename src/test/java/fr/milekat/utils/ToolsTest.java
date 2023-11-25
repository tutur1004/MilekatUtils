package fr.milekat.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(i, Tools.getInt(Tools.getString(i)));
        }
    }

    @Test
    public void testGetInt() {
        Assertions.assertEquals(0, Tools.getInt(":zero:"));
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(i, Tools.getInt(Tools.getString(i)));
        }
    }

    @Test
    public void testGetRandomString() {
        int length = 10;
        String randomString = Tools.getRandomString(length);
        Assertions.assertEquals(length, randomString.length());
    }

    @Test
    public void testToByteArray() throws IOException {
        // Test data
        String testData = "Test data for input stream";
        byte[] testDataBytes = testData.getBytes();

        // Create a ByteArrayInputStream from the test data
        InputStream testInputStream = new ByteArrayInputStream(testDataBytes);

        // Use the toByteArray method to convert the InputStream
        byte[] result = Tools.toByteArray(testInputStream);

        // Create a reference byte array using a ByteArrayOutputStream
        testInputStream = new ByteArrayInputStream(testDataBytes);
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = testInputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        byte[] reference = output.toByteArray();

        // Compare the result with the reference byte array
        Assertions.assertArrayEquals(reference, result);
    }

}

