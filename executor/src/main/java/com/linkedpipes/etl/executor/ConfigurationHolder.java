package com.linkedpipes.etl.executor;

import com.linkedpipes.etl.executor.cli.Configuration;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ConfigurationHolder {

    private static Configuration configuration;

    public static void setConfiguration(Configuration configuration) {
        ConfigurationHolder.configuration = configuration;
    }

    public Integer getWebServerPort() {
        return configuration.httpPort();
    }

    public String getOsgiStorageDirectory() {
        return configuration.osgiWorkingDirectory();
    }

    public File getOsgiLibDirectory() {
        return new File(configuration.osgiLibrariesDirectory());
    }

    public File getOsgiComponentDirectory() {
        return new File(configuration.pluginsDirectory());
    }

    public String getStorageAddress() {
        return configuration.storageUrl();
    }

    public List<String> getBannedJarPatterns() {
        return configuration.bannedPluginIriPatterns();
    }

}
