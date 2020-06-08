package com.linkedpipes.etl.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Service
public class Configuration {

    private static final Logger LOG
            = LoggerFactory.getLogger(Configuration.class);

    private int webServerPort;

    private String logDirectoryPath;

    private String logCoreFilter;

    private String osgiLibDirectoryPath;

    private String osgiStorageDirectory;

    private String osgiComponentDirectory;

    private String storageAddress;

    private final List<String> bannedJarPatterns = new ArrayList<>(20);

    private final Properties properties = new Properties();

    @PostConstruct
    public void init() {
        final String propertiesFile = System.getProperty("configFileLocation");
        if (propertiesFile == null) {
            LOG.error("Missing property '-configFileLocation' "
                    + "with path to configuration file.");
            throw new RuntimeException("Missing configuration file.");
        }
        LOG.info("Reading configuration file: {}", propertiesFile);
        // Read properties.
        try (InputStreamReader stream = new InputStreamReader(
                new FileInputStream(new File(propertiesFile)),"UTF8")) {
            properties.load(stream);
        } catch (IOException ex) {
            throw new RuntimeException("Can't load configuration file.", ex);
        }
        // Load properties.
        loadProperties();
    }

    protected void loadProperties() {
        webServerPort = getPropertyInteger("executor.webserver.port");
        logDirectoryPath = getProperty("executor.log.directory");
        logCoreFilter = getProperty("executor.log.core.level");
        osgiLibDirectoryPath = getProperty("executor.osgi.lib.directory");
        osgiStorageDirectory = getProperty("executor.osgi.working.directory");
        osgiComponentDirectory = getProperty("storage.jars.directory");
        storageAddress = getProperty("storage.uri");
        //
        try {
            String value = properties.getProperty(
                    "executor.banned_jar_iri_patterns")
                    .replaceAll("\\\"", "\"").trim();
            // Remove first and last "
            value = value.substring(1, value.length() - 1);
            this.bannedJarPatterns.addAll(
                    Arrays.asList(value.split("\",\"")));
        } catch (RuntimeException ex) {
            // This property is obligatory.
        }
        //
        validateDirectory(logDirectoryPath);
        validateDirectory(osgiLibDirectoryPath);
        validateDirectory(osgiStorageDirectory);
        validateDirectory(osgiComponentDirectory);
    }

    public File getLogDirectory() {
        final File logDirectory = new File(logDirectoryPath);
        logDirectory.mkdirs();
        return logDirectory;
    }

    public int getWebServerPort() {
        return webServerPort;
    }

    public String getLogCoreFilter() {
        return logCoreFilter;
    }

    public File getOsgiLibDirectory() {
        return new File(osgiLibDirectoryPath);
    }

    public String getOsgiStorageDirectory() {
        return osgiStorageDirectory;
    }

    public File getOsgiComponentDirectory() {
        return new File(osgiComponentDirectory);
    }

    public String getStorageAddress() {
        return storageAddress;
    }

    public List<String> getBannedJarPatterns() {
        return bannedJarPatterns;
    }

    private String getProperty(String name) {
        final String value;
        try {
            value = properties.getProperty(name);
        } catch (RuntimeException ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw ex;
        }
        if (value == null) {
            LOG.error("Missing configuration property: '{}'", name);
            throw new RuntimeException("Missing configuration property!");
        } else {
            return value;
        }
    }

    private Integer getPropertyInteger(String name) {
        final String value = getProperty(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
    }

    private static void validateDirectory(String value) {
        (new File(value)).mkdirs();
    }

}
