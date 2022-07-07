package com.linkedpipes.etl.executor.cli.adapter;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.cli.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class PropertiesToConfiguration {

    public static Configuration updateConfiguration(
            Configuration defaults, File file)
            throws ExecutorException {
        Properties properties = new Properties();
        try (var stream = new FileInputStream(file);
             var reader = new InputStreamReader(stream,
                     StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException ex) {
            throw new ExecutorException("Can't load configuration file.", ex);
        }
        //
        return defaults.merge(
                getInteger(properties, "executor.webserver.port"),
                getString(properties, "executor.execution.working_directory"),
                getString(properties, "executor.log.directory"),
                getString(properties, "executor.log.core.level"),
                getString(properties, "executor.osgi.working.directory"),
                getString(properties, "executor.osgi.lib.directory"),
                getString(properties, "storage.jars.directory"),
                getList(properties, "executor.banned_jar_iri_patterns")
        );
    }

    private static String getString(
            Properties properties, String name) throws ExecutorException {
        try {
            return properties.getProperty(name);
        } catch (RuntimeException ex) {
            throw new ExecutorException(
                    "Invalid configuration property: '{}'", name, ex);
        }
    }

    private static Integer getInteger(
            Properties properties, String name) throws ExecutorException {
        String value = getString(properties, name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            throw new ExecutorException(
                    "Invalid configuration property: '{}'", name);
        }
    }

    private static List<String> getList(
            Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            return Collections.emptyList();
        }
        value = value.replaceAll("\\\"", "\"").trim();
        if (value.isEmpty() || value.isBlank()) {
            return Collections.emptyList();
        }
        // Remove first and last double quote.
        value = value.substring(1, value.length() - 1);
        return Arrays.asList(value.split("\",\""));
    }

}
