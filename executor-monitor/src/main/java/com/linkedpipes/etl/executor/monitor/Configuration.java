package com.linkedpipes.etl.executor.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

@Service
public class Configuration {

    private static final Logger LOG
            = LoggerFactory.getLogger(Configuration.class);

    private String workingDirectoryPath;

    private int webServerPort;

    private String logDirectoryPath;

    private String logFilter;

    private String executorUri;

    private int ftpCommandPort;

    private String executionPrefix;

    private int ftpDataPortsStart;

    private int ftpDataPortsEnd;

    private final Properties properties = new Properties();

    private String slackFinishedWebhook;

    private String slackErrorWebhook;

    private String localUrl;

    private String publicWorkingDataUrlPrefix;

    /**
     * If true dangling executions should be re-executed.
     */
    private boolean retryExecution = false;

    /**
     * Mull for no limit.
     */
    private Integer retryLimit = null;

    private Integer historyLimit = null;

    private Integer historyHourLimit = null;

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
                new FileInputStream(new File(propertiesFile)), "UTF8")) {
            properties.load(stream);
        } catch (IOException ex) {
            throw new RuntimeException("Can't load configuration file.", ex);
        }
        // Load properties.
        loadProperties();
    }

    protected void loadProperties() {
        executorUri = getEnvOrProperty(
                "LP_ETL_EXECUTOR_URL","executor.webserver.uri");
        workingDirectoryPath = getProperty(
                "executor.execution.working_directory");
        webServerPort = getPropertyInteger("executor-monitor.webserver.port");
        logDirectoryPath = getProperty("executor-monitor.log.directory");
        logFilter = getProperty("executor-monitor.log.core.level");
        ftpCommandPort = getPropertyInteger(
                "executor-monitor.ftp.command_port");
        //
        ftpDataPortsStart = getPropertyInteger(
                "executor-monitor.ftp.data_ports_interval.start");
        ftpDataPortsEnd = getPropertyInteger(
                "executor-monitor.ftp.data_ports_interval.end");
        slackFinishedWebhook = getOptionalProperty(
                "executor-monitor.slack_finished_executions_webhook");
        slackErrorWebhook = getOptionalProperty(
                "executor-monitor.slack_error_webhook");
        localUrl = getEnvOrProperty("LP_ETL_DOMAIN", "domain.uri");
        executionPrefix = localUrl + "/resources/executions/";
        publicWorkingDataUrlPrefix = getOptionalProperty(
                "executor-monitor.public_working_data_url_prefix");

        retryLimit = getOptionalPropertyInteger(
                "executor-monitor.retry_limit");
        retryExecution = retryLimit != null;

        historyLimit = getEnvOrOptionalPropertyInteger(
                "LP_ETL_EXECUTION_HISTORY_COUNT_LIMIT",
                "executor-monitor.history_limit");

        historyLimit = getEnvOrOptionalPropertyInteger(
                "LP_ETL_EXECUTION_HISTORY_HOUR_LIMIT",
                "executor-monitor.history_hour_limit");

        //
        validateUri(executorUri, "executor.execution.working_directory");
        validateDirectory(workingDirectoryPath);
        validateDirectory(logDirectoryPath);
    }

    public File getRawWorkingDirectory() {
        return new File(workingDirectoryPath);
    }

    public File getWorkingDirectory() {
        File workingDirectory =
                new File(workingDirectoryPath + File.separator + "data");
        workingDirectory.mkdirs();
        return workingDirectory;
    }

    public File getLogDirectory() {
        File logDirectory = new File(logDirectoryPath);
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

    public int getFtpCommandPort() {
        return ftpCommandPort;
    }

    public String getExecutionPrefix() {
        return executionPrefix;
    }

    public int getFtpDataPortsStart() {
        return ftpDataPortsStart;
    }

    public int getFtpDataPortsEnd() {
        return ftpDataPortsEnd;
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
        String value;
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

    private String getEnvOrProperty(String env, String name) {
        String value = System.getenv(env);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return getProperty(name);
    }

    private String getOptionalProperty(String name) {
        String value;
        try {
            value = properties.getProperty(name);
        } catch (RuntimeException ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw ex;
        }
        if (value == null) {
            return null;
        }
        if (value.equals("")) {
            value = null;
        }
        return value;
    }

    protected Integer getEnvOrOptionalPropertyInteger(String env, String name) {
        String value = System.getenv(env);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (Exception ex) {
                LOG.error("Invalid configuration property: '{}'", name);
                throw new RuntimeException(ex);
            }
        }
        return getOptionalPropertyInteger(name);
    }

    protected Integer getOptionalPropertyInteger(String name) {
        String value;
        try {
            value = properties.getProperty(name);
        } catch (RuntimeException ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw ex;
        }
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
    }

    protected int getPropertyInteger(String name) {
        String value = getProperty(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
    }

    public String getSlackFinishedWebhook() {
        return slackFinishedWebhook;
    }

    public String getSlackErrorWebhook() {
        return slackErrorWebhook;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public String getPublicWorkingDataUrlPrefix() {
        return publicWorkingDataUrlPrefix;
    }

    public boolean isRetryExecution() {
        return retryExecution;
    }

    public Integer getRetryLimit() {
        return retryLimit;
    }

    public Integer getHistoryLimit() {
        return historyLimit;
    }

    public Integer getHistoryHourLimit() {
        return historyHourLimit;
    }

}
