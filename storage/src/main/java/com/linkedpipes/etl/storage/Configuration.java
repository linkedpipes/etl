package com.linkedpipes.etl.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Configuration {

    private static final Logger LOG
            = LoggerFactory.getLogger(Configuration.class);

    private int storageHttpPort;

    private String logDirectoryPath;

    private String logCoreFilter;

    private String jarDirectory;

    private String domainName;

    private String storageDirectory;

    private String executorMonitorUrl;

    private final Properties properties = new Properties();

    public void initialize() {
        String propertiesFile = System.getProperty("configFileLocation");
        if (propertiesFile == null) {
            LOG.error("Missing property '-configFileLocation' "
                    + "with path to configuration file.");
            throw new RuntimeException("Missing configuration file.");
        }
        LOG.debug("Reading configuration file: {}", propertiesFile);
        // Read properties.
        try (InputStreamReader stream = new InputStreamReader(
                new FileInputStream(propertiesFile),
                StandardCharsets.UTF_8)) {
            properties.load(stream);
        } catch (IOException ex) {
            throw new RuntimeException("Can't load configuration file.", ex);
        }
        // Load properties.
        loadProperties();
    }

    protected void loadProperties() {
        storageHttpPort = getPropertyInteger("storage.port");
        logDirectoryPath = getProperty("storage.log.directory");
        logCoreFilter = getProperty("storage.log.core.level");

        jarDirectory = getProperty("storage.jars.directory");
        domainName = getEnvOrProperty("LP_ETL_DOMAIN", "domain.uri");

        storageDirectory = getProperty("storage.directory");

        executorMonitorUrl = getEnvOrProperty(
                "LP_ETL_MONITOR_URL",
                "executor-monitor.webserver.uri");

        // Update loaded properties.

        executorMonitorUrl += "/api/v1/";
    }

    private String getProperty(String name) {
        String value;
        try {
            value = properties.getProperty(name);
        } catch (RuntimeException ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw ex;
        }
        if (value == null) {
            LOG.error("Missing configuration property: '{}'", name);
            throw new RuntimeException(
                    "Missing configuration property:" + name);
        } else {
            return value;
        }
    }

    private String getEnvOrProperty(String env, String name) {
        String value = System.getenv(env);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return getProperty(name);
    }

    protected Integer getPropertyInteger(String name) {
        String value = getProperty(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
    }

    public int getStorageHttpPort() {
        return storageHttpPort;
    }

    public File getLogDirectory() {
        return new File(logDirectoryPath);
    }

    public String getLogCoreFilter() {
        return logCoreFilter;
    }

    public File getJavaPluginsDirectory() {
        return new File(jarDirectory);
    }

    public File getStorageDirectory() {
        return new File(storageDirectory);
    }

    public String getDomainName() {
        return domainName;
    }

    public String getExecutorMonitorUrl() {
        return executorMonitorUrl;
    }

}
