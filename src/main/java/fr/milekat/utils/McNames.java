package fr.milekat.utils;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

@SuppressWarnings("unused")
public class McNames {
    /**
     *      Simple tool to get a string UUID from a Minecraft name (If exist)
     */
    public static @NotNull String getUuid(String name) throws IOException {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        try {
            @SuppressWarnings("deprecation")
            String UUIDJson = IOUtils.toString(new URL(url));
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
     *      Simple tool to get a Minecraft name from a string UUID (If exist)
     */
    public static String getName(@NotNull String uuid) {
        String url = "https://api.mojang.com/user/profile/" + uuid;
        try {
            @SuppressWarnings("deprecation")
            String nameJson = IOUtils.toString(new URL(url));
            if (nameJson.isEmpty()) return "invalid uuid";
            JSONObject UUIDObject = new JSONObject(nameJson);
            return UUIDObject.get("name").toString();
        } catch (IOException | JSONException exception) {
            exception.printStackTrace();
        }
        return "error";
    }
}
