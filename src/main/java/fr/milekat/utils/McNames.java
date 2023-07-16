package fr.milekat.utils;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

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
        try {
            String UUIDJson = IOUtils.toString(new URL(url), Charset.defaultCharset());
            if (UUIDJson.isEmpty()) return "invalid name";
            JSONObject UUIDObject = new JSONObject(UUIDJson);
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
        try {
            String nameJson = IOUtils.toString(new URL(url), Charset.defaultCharset());
            if (nameJson.isEmpty()) return "invalid uuid";
            JSONObject UUIDObject = new JSONObject(nameJson);
            return UUIDObject.get("name").toString();
        } catch (IOException | JSONException exception) {
            exception.printStackTrace();
        }
        return "error";
    }
}
