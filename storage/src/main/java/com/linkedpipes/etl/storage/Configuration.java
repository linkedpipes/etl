package com.linkedpipes.etl.storage;

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

    private int storagePort;

    private String logDirectoryPath;

    private String logCoreFilter;

    private String jarDirectory;

    private String templatesDirectory;

    private String pipelinesDirectory;

    private String knowledgeDirectory;

    private String domainName;

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
        storagePort = getPropertyInteger("storage.port");
        logDirectoryPath = getProperty("storage.log.directory");
        logCoreFilter = getProperty("storage.log.core.level");

        jarDirectory = getProperty("storage.jars.directory");
        domainName = getProperty("domain.uri");

        String storageDirectory = getProperty("storage.directory");
        templatesDirectory = storageDirectory + File.separator + "templates";
        pipelinesDirectory = storageDirectory + File.separator + "pipelines";
        knowledgeDirectory = storageDirectory + File.separator + "knowledge";
    }

    public int getStoragePort() {
        return storagePort;
    }

    public File getLogDirectory() {
        final File logDirectory = new File(logDirectoryPath);
        logDirectory.mkdirs();
        return logDirectory;
    }

    public String getLogCoreFilter() {
        return logCoreFilter;
    }

    public File getJarDirectory() {
        return new File(jarDirectory);
    }

    public File getTemplatesDirectory() {
        return new File(templatesDirectory);
    }

    public File getPipelinesDirectory() {
        return new File(pipelinesDirectory);
    }

    public File getKnowledgeDirectory() {
        return new File(knowledgeDirectory);
    }

    public String getDomainName() {
        return domainName;
    }

    public Properties getProperties() {
        return properties;
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
            throw new RuntimeException("Missing configuration property:" +
                    name);
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
