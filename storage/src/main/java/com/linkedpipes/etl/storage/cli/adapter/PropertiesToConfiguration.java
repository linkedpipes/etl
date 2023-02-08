package com.linkedpipes.etl.storage.cli.adapter;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.cli.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertiesToConfiguration {

    private final Properties properties;

    public static Configuration updateConfiguration(
            Configuration defaults, File file)
            throws StorageException {
        PropertiesToConfiguration instance = new PropertiesToConfiguration(
                loadProperties(file));
        //
        Configuration next = new Configuration();
        next.httpPort = instance.getInteger(
                "storage.port");
        next.dataDirectory = instance.getString(
                "storage.directory");
        next.logDirectory = instance.getString(
                "storage.log.directory");
        next.logLevel = instance.getString(
                "storage.log.core.level");
        next.baseUrl = instance.getString(
                "domain.uri");
        next.pluginDirectory = instance.getString(
                "storage.jars.directory");
        next.executorMonitorUrl = instance.getString(
                "executor-monitor.webserver.uri");
        return defaults.merge(next);
    }

    private static Properties loadProperties(File file)
            throws StorageException {
        Properties properties = new Properties();
        try (var stream = new FileInputStream(file);
             var reader = new InputStreamReader(stream,
                     StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException ex) {
            throw new StorageException("Can't load configuration file.", ex);
        }
        return properties;
    }

    private PropertiesToConfiguration(Properties properties) {
        this.properties = properties;
    }

    private String getString(String name) throws StorageException {
        try {
            return properties.getProperty(name);
        } catch (RuntimeException ex) {
            throw new StorageException(
                    "Invalid configuration property: '{}'", name, ex);
        }
    }

    private Integer getInteger(String name) throws StorageException {
        String value = getString(name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            throw new StorageException(
                    "Invalid configuration property: '{}'", name);
        }
    }

}
