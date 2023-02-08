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

    private final Properties properties;

    public static Configuration updateConfiguration(
            Configuration defaults, File file)
            throws ExecutorException {
        PropertiesToConfiguration instance = new PropertiesToConfiguration(
                loadProperties(file));
        //
        Configuration next = new Configuration();
        next.httpPort = instance.getInteger(
                "executor.webserver.port");
        next.dataDirectory = instance.getString(
                "executor.execution.working_directory");
        next.logDirectory  = instance.getString(
                "executor.log.directory");
        next.logLevel = instance.getString(
                "executor.log.core.level");
        next.osgiWorkingDirectory = instance.getString(
                "executor.osgi.working.directory");
        next.osgiLibrariesDirectory = instance.getString(
                "executor.osgi.lib.directory");
        next.pluginsDirectory = instance.getString(
                "storage.jars.directory");
        next.bannedPluginIriPatterns = instance.getList(
                "executor.banned_jar_iri_patterns");

        return defaults.merge(next);
    }

    private static Properties loadProperties(File file)
            throws ExecutorException {
        Properties properties = new Properties();
        try (var stream = new FileInputStream(file);
             var reader = new InputStreamReader(stream,
                     StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException ex) {
            throw new ExecutorException("Can't load configuration file.", ex);
        }
        return properties;
    }

    private PropertiesToConfiguration(Properties properties) {
        this.properties = properties;
    }

    private String getString(String name) throws ExecutorException {
        try {
            return properties.getProperty(name);
        } catch (RuntimeException ex) {
            throw new ExecutorException(
                    "Invalid configuration property: '{}'", name, ex);
        }
    }

    private Integer getInteger(String name) throws ExecutorException {
        String value = getString(name);
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

    private List<String> getList(String name) {
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
