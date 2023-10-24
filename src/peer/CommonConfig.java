package src.peer;

import java.util.*;
import java.io.IOException;
import java.nio.file.*;

// Reads key value pairs from file and store into a map
public class CommonConfig {

    // Create a Map named properties to store config data as key value pair
    private final Map<String, String> properties = new HashMap<>();

    // Reads config data from file
    public CommonConfig(String filename) throws IOException {

        // Reads all lines
        List<String> lines = Files.readAllLines(Paths.get(filename));

        // Adds key into parts[0] and value into parts[1] in the lines list
        for (String line : lines) {
            String[] parts = line.split(" ");
            properties.put(parts[0], parts[1]);
        }
    }

    // Return value of config based on the key
    public String getProperty(String key) {
        return properties.get(key);

    }
}
