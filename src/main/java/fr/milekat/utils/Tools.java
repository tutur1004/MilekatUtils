package fr.milekat.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@SuppressWarnings("unused")
public class Tools {
    /**
     * Remove the last character from a string.
     *
     * @param str The input string.
     * @return The string with the last character removed.
     */
    public static String remLastChar(String str) {
        return Optional.ofNullable(str)
                .filter(sStr -> !sStr.isEmpty())
                .map(sStr -> sStr.substring(0, sStr.length() - 1))
                .orElse(str);
    }

    /**
     * Check if a string is composed only of alphabetic, numeric, or specific characters.
     *
     * @param str The input string.
     * @return true if the string is alphanumeric, false otherwise.
     */
    public static boolean isAlphaNumericExtended(String str) {
        return str != null && str.matches("^[a-zA-Z0-9\\s()_éèêïç\\-]*$");
    }

    /**
     * Converts an integer to the corresponding emoji unicode.
     *
     * @param number The input number.
     * @return The emoji unicode representing the number.
     */
    @Contract(pure = true)
    public static @NotNull String getString(int number) {
        switch (number) {
            case 1: {
                return "1️⃣";
            }
            case 2: {
                return "2️⃣";
            }
            case 3: {
                return "3️⃣";
            }
            case 4: {
                return "4️⃣";
            }
            case 5: {
                return "5️⃣";
            }
            case 6: {
                return "6️⃣";
            }
            case 7: {
                return "7️⃣";
            }
            case 8: {
                return "8️⃣";
            }
            case 9: {
                return "9️⃣";
            }
        }
        return ":zero:";
    }

    /**
     * Converts an emoji unicode to the corresponding integer.
     *
     * @param number The input emoji unicode.
     * @return The integer represented by the emoji.
     */
    @Contract(pure = true)
    public static int getInt(@NotNull String number) {
        switch (number) {
            case "1️⃣": {
                return 1;
            }
            case "2️⃣": {
                return 2;
            }
            case "3️⃣": {
                return 3;
            }
            case "4️⃣": {
                return 4;
            }
            case "5️⃣": {
                return 5;
            }
            case "6️⃣": {
                return 6;
            }
            case "7️⃣": {
                return 7;
            }
            case "8️⃣": {
                return 8;
            }
            case "9️⃣": {
                return 9;
            }
        }
        return 0;
    }

    /**
     * Generates a random string of the specified length.
     *
     * @param resultLength The length of the random string to generate.
     * @return The random string.
     */
    public static @NotNull String getRandomString(int resultLength) {
        // Define the character set for generating random string
        @SuppressWarnings("SpellCheckingInspection")
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "0123456789" +
                "abcdefghijklmnopqrstuvxyz";
        // Get the length of the character set
        final int length = characters.length();

        // Create a StringBuilder to construct the random string
        StringBuilder sb = new StringBuilder(resultLength);
        for (int i = 0; i < resultLength; i++) {
            // Generate a random index within the range of the character set length
            int randomIndex = (int) (Math.random() * length);
            // Append the character at the random index to the StringBuilder
            sb.append(characters.charAt(randomIndex));
        }
        // Convert the StringBuilder to a string and return the random string
        return sb.toString();
    }


    /**
     * Converts an InputStream to a byte array.
     *
     * @param input The InputStream to convert.
     * @return The byte array representing the InputStream content.
     * @throws IOException If an I/O error occurs.
     */
    public static byte @NotNull [] toByteArray(@NotNull InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }
}
