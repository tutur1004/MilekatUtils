package fr.milekat.utils.storage.utils;

import org.jetbrains.annotations.NotNull;

public class Tools {
    public static @NotNull String hideSecret(@NotNull String password) {
        int passLength = password.length();
        if (passLength <= 12) {
            return "*".repeat(passLength);
        } else {
            return password.substring(0, 10) + "*".repeat(passLength - 10);
        }
    }
}
