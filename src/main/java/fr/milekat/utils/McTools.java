package fr.milekat.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class McTools {
    /**
     * Formats the list of arguments for Minecraft tab completion.
     *
     * @param arg       The input argument.
     * @param MyStrings The list of strings to filter and format.
     * @return The sorted list of strings that start with the input argument.
     */
    public static @NotNull ArrayList<String> getTabArgs(String arg, @NotNull List<String> MyStrings) {
        ArrayList<String> MySortStrings = new ArrayList<>();
        for(String loop : MyStrings) {
            if(loop.toLowerCase().startsWith(arg.toLowerCase()))
            {
                MySortStrings.add(loop);
            }
        }
        return MySortStrings;
    }

    /**
     * Translates Minecraft color codes in a text string.
     *
     * @param textToTranslate The text to translate.
     * @return The translated text with Minecraft color codes.
     * @author From net.md_5.bungee.api.ChatColor#translateAlternateColorCodes()
     */
    @Contract("_ -> new")
    public static @NotNull String minecraftColorCodes(@NotNull String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
            if (b[i] == '&' && ALL_CODES.indexOf(b[i + 1]) > -1) {
                char COLOR_CHAR = (char) 167;
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
