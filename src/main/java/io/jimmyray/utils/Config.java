package io.jimmyray.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Static config properties access point
 */
public final class Config  {
    private static final String propertiesFile = "config.properties";
    public static final Properties properties;

    static {
        // create a reader object on the properties file
        FileReader reader = null;
        try {
            reader = new FileReader(Config.propertiesFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // create properties object
        properties = new Properties();

        // Add a wrapper around reader object
        try {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
