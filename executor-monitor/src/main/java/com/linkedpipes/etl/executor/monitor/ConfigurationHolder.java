package com.linkedpipes.etl.executor.monitor;

import com.linkedpipes.etl.executor.monitor.cli.Configuration;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Wrap the configuration, as we do not provide direct access for services.
 */
@Service
public class ConfigurationHolder {

    private static Configuration configuration;

    public static void setConfiguration(Configuration configuration) {
        ConfigurationHolder.configuration = configuration;
    }

    public File getRawWorkingDirectory() {
        return new File(configuration.dataDirectory);
    }

    public File getWorkingDirectory() {
        return new File(
                configuration.dataDirectory + File.separator + "data");
    }

    public int getWebServerPort() {
        return configuration.httpPort;
    }

    public String getExecutorUri() {
        return configuration.executorUrl;
    }

    public int getFtpCommandPort() {
        return configuration.ftpCommandPort;
    }

    public String getExecutionPrefix() {
        return configuration.baseUrl + "/resources/executions/";
    }

    public int getFtpDataPortsStart() {
        return configuration.ftpDataPortsStart;
    }

    public int getFtpDataPortsEnd() {
        return configuration.ftpDataPortsEnd;
    }

    public String getSlackFinishedWebhook() {
        return configuration.slackFinishedWebhook;
    }

    public String getSlackErrorWebhook() {
        return configuration.slackErrorWebhook;
    }

    public String getLocalUrl() {
        return configuration.baseUrl;
    }

    public String getPublicWorkingDataUrlPrefix() {
        return configuration.publicWorkingDataUrlPrefix;
    }

    public boolean isRetryExecution() {
        return configuration.danglingRetryLimit != null;
    }

    public Integer getRetryLimit() {
        return configuration.danglingRetryLimit;
    }

    public Integer getHistoryLimit() {
        return configuration.historyLimit;
    }

    public Integer getHistoryHourLimit() {
        return configuration.historyHourLimit;
    }

}
