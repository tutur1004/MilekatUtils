package fr.milekat.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class McToolsTest {

    @Test
    void testGetTabArgs() {
        List<String> myStrings = Arrays.asList("apple", "apricot", "banana", "orange", "pear");
        ArrayList<String> expected = new ArrayList<>(Arrays.asList("apple", "apricot"));
        ArrayList<String> actual = McTools.getTabArgs("a", myStrings);
        Assertions.assertEquals(expected, actual);

        expected = new ArrayList<>(Collections.singletonList("banana"));
        actual = McTools.getTabArgs("b", myStrings);
        Assertions.assertEquals(expected, actual);

        expected = new ArrayList<>();
        actual = McTools.getTabArgs("z", myStrings);
        Assertions.assertEquals(expected, actual);

        // Test with empty list
        expected = new ArrayList<>();
        actual = McTools.getTabArgs("a", Collections.emptyList());
        Assertions.assertEquals(expected, actual);
    }

}

