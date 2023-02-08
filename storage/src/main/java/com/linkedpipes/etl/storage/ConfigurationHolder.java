package com.linkedpipes.etl.storage;

import com.linkedpipes.etl.storage.cli.Configuration;
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

    public Integer getWebServerPort() {
        return configuration.httpPort;
    }

    public File getJavaPluginsDirectory() {
        return new File(configuration.pluginDirectory);
    }

    public File getStorageDirectory() {
        return new File(configuration.dataDirectory);
    }

    public String getDomainName() {
        return configuration.baseUrl;
    }

    public String getExecutorMonitorUrl() {
        return configuration.executorMonitorUrl;
    }

}
