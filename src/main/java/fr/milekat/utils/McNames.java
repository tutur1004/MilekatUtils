package fr.milekat.utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class McNames {
    /**
     * Retrieves the UUID of a Minecraft player from their name (if it exists).
     *
     * @param name The Minecraft player name.
     * @return The UUID of the player in the format xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.
     * @throws IOException If an I/O error occurs.
     */
    public static @NotNull String getUuid(String name) throws IOException {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8))) {
            // Read the content of the URL
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            // Ensure the content is not empty
            if (content.length() == 0) {
                return "invalid name";
            }

            // Parse the JSON content
            JSONObject UUIDObject = new JSONObject(content.toString());

            // Return the UUID
            return UUIDObject.get("id").toString().replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5");
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return "error";
    }

    /**
     * Retrieves the Minecraft player name from their UUID (if it exists).
     *
     * @param uuid The UUID of the player.
     * @return The Minecraft player name.
     */
    public static String getName(@NotNull String uuid) {
        String url = "https://api.mojang.com/user/profile/" + uuid;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8))) {
            // Read the content of the URL
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            // Ensure the content is not empty
            if (content.length() == 0) {
                return "invalid uuid";
            }

            // Parse the JSON content
            JSONObject UUIDObject = new JSONObject(content.toString());

            // Return the player name
            return UUIDObject.get("name").toString();
        } catch (IOException | JSONException exception) {
            exception.printStackTrace();
        }
        return "error";
    }
}
