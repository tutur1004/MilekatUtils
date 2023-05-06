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
     * Remove the last char of string
     * <br>
     * Source: <a href="https://www.xenovation.com/blog/development/java/remove-last-character-from-string-java">xenovation post</a>
     */
    public static String remLastChar(String str) {
        return Optional.ofNullable(str)
                .filter(sStr -> sStr.length() != 0)
                .map(sStr -> sStr.substring(0, sStr.length() - 1))
                .orElse(str);
    }

    /**
     * Check if string is only composed by Alpha, Num or specific characters
     */
    public static boolean isAlphaNumericExtended(String str) {
        return str != null && str.matches("^[a-zA-Z0-9\\s\\(\\)\\_\\é\\è\\ê\\ï\\ç\\-]*$");
    }

    /**
     * Int to emoji unicode
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
     * Emoji unicode to int
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
     * Get a string full randomly
     */
    public static @NotNull String getRandomString(int n) {
        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());
            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }

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
