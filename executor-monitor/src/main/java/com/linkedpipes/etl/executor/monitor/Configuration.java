package com.linkedpipes.etl.executor.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 *
 * @author Škoda Petr
 */
@Service
public class Configuration {

    private static final Logger LOG
            = LoggerFactory.getLogger(Configuration.class);

    private String workingDirectoryPath;

    private int webServerPort;

    private String logDirectoryPath;

    private String logFilter;

    private String executorUri;

    private String fusekiPath = null;

    private String externalWorkingDirectoryPath;

    private int ftpServerPort;

    private int processPortStart;

    private int processPortEnd;

    private String executionPrefix;

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
        try (InputStream stream = new FileInputStream(new File(propertiesFile))) {
            properties.load(stream);
        } catch (IOException ex) {
            throw new RuntimeException("Can't load configuration file.", ex);
        }
        // Load properties.
        loadProperties();
    }

    protected void loadProperties() {
        executorUri = getProperty("executor.webserver.uri");
        workingDirectoryPath = getProperty("executor.execution.working_directory");
        webServerPort = getPropertyInteger("executor-monitor.webserver.port");
        logDirectoryPath = getProperty("executor-monitor.log.directory");
        logFilter = getProperty("executor-monitor.log.core.level");
        fusekiPath = getProperty("external.fuseki.path");
        externalWorkingDirectoryPath = getProperty("external.working");
        ftpServerPort = getPropertyInteger("executor-monitor.ftp.port");
        processPortStart = getPropertyInteger("external.port.start");
        processPortEnd = getPropertyInteger("external.port.end");
        executionPrefix = getProperty("executor.execution.uriPrefix");
        //
        validateUri(executorUri, "executor.execution.working_directory");
        validateDirectory(workingDirectoryPath);
        validateDirectory(logDirectoryPath);
    }

    public File getWorkingDirectory() {
        final File workingDirectory = new File(workingDirectoryPath + File.separator + "data");
        workingDirectory.mkdirs();
        return workingDirectory;
    }

    public File getUploadDirectory() {
        final File uploadDirectory = new File(workingDirectoryPath + File.separator + "upload");
        uploadDirectory.mkdirs();
        return uploadDirectory;
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
        return logFilter;
    }

    public String getExecutorUri() {
        return executorUri;
    }

    public File getFusekiPath() {
        return new File(fusekiPath);
    }

    public File getExternalWorkingDirectoryPath() {
        final File result = new File(externalWorkingDirectoryPath);
        result.mkdirs();
        return result;
    }

    public int getFtpServerPort() {
        return ftpServerPort;
    }

    public int getProcessPortStart() {
        return processPortStart;
    }

    public int getProcessPortEnd() {
        return processPortEnd;
    }

    public String getExecutionPrefix() {
        return executionPrefix;
    }

    protected void validateUri(String value, String name) {
        try {
            new URI(value);
        } catch (URISyntaxException ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
    }

    protected void validateDirectory(String value) {
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
