package com.digitallib.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    public static String getProperty(String key) {
        String configFilePath = "config.properties";
        File configFile = new File(configFilePath);

        if (!configFile.exists()) {
            boolean created = false;
            try {
                created = configFile.createNewFile();
                if (created) {
                    System.out.println("New configuration file created: " + configFilePath);
                } else {
                    System.err.println("Failed to create configuration file.");
                    return null;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Properties properties = new Properties();
        try (FileReader reader = new FileReader(configFile)) {
            properties.load(reader);
            return properties.getProperty(key);
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage());
        }

        return null;
    }

    public static void setProperty(String key, String value) {
        String configFilePath = "config.properties";
        File configFile = new File(configFilePath);

        if (configFile.exists()) {
            Properties properties = new Properties();
            try (FileReader reader = new FileReader(configFile)) {
                properties.load(reader);
                properties.setProperty(key, value);

                try (FileWriter writer = new FileWriter(configFile)) {
                    properties.store(writer, "Updated Configuration");
                }
            } catch (IOException e) {
                System.err.println("Error saving config file: " + e.getMessage());
            }
        }
    }
}
