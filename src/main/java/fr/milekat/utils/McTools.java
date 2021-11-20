package fr.milekat.utils;

import java.util.ArrayList;
import java.util.List;

public class McTools {
    /**
     * Format list of args for Mc tab,
     */
    public static ArrayList<String> getTabArgs(String arg, List<String> MyStrings) {
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
