package com.linkedpipes.etl.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private String executorMonitorUrl;

    private final Properties properties = new Properties();

    @PostConstruct
    public void init() {
        String propertiesFile = System.getProperty("configFileLocation");
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
        storagePort = getPropertyInteger("storage.port");
        logDirectoryPath = getProperty("storage.log.directory");
        logCoreFilter = getProperty("storage.log.core.level");

        jarDirectory = getProperty("storage.jars.directory");
        domainName = getEnvOrProperty("LP_ETL_DOMAIN", "domain.uri");

        String storageDirectory = getProperty("storage.directory");
        templatesDirectory = storageDirectory + File.separator + "templates";
        pipelinesDirectory = storageDirectory + File.separator + "pipelines";
        knowledgeDirectory = storageDirectory + File.separator + "knowledge";

        executorMonitorUrl =
                getProperty("executor-monitor.webserver.uri") + "/api/v1/";
    }

    public int getStoragePort() {
        return storagePort;
    }

    public File getLogDirectory() {
        File logDirectory = new File(logDirectoryPath);
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

    public String getExecutorMonitorUrl() {
        return executorMonitorUrl;
    }

    public Properties getProperties() {
        return properties;
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
            Integer valueAsInteger = Integer.parseInt(value);
            return valueAsInteger;
        } catch (Exception ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
    }

}
