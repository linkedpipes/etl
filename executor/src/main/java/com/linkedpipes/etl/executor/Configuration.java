package com.linkedpipes.etl.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private String storageAddress;

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
        try (InputStream stream = new FileInputStream(
                new File(propertiesFile))) {
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
        storageAddress = getProperty("storage.uri");
        //
        validateDirectory(logDirectoryPath);
        validateDirectory(osgiLibDirectoryPath);
        validateDirectory(osgiStorageDirectory);
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

    public String getStorageAddress() {
        return storageAddress;
    }

    private static void validateDirectory(String value) {
        (new File(value)).mkdirs();
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

    protected Integer getPropertyInteger(String name) {
        final String value = getProperty(name);
        try {
            final Integer valueAsInteger = Integer.parseInt(value);
            return valueAsInteger;
        } catch (Exception ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
    }

}
