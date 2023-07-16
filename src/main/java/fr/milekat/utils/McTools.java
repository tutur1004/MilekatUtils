package fr.milekat.utils;

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
}
