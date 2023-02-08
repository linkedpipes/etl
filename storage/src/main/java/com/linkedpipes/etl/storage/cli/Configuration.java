package com.linkedpipes.etl.storage.cli;

public class Configuration {

    /**
     * HTTP server port.
     */
    public Integer httpPort;

    /**
     * Directory where storage should store data.
     */
    public String dataDirectory;

    /**
     * Directory where to store logs.
     */
    public String logDirectory;

    /**
     * Log level to employ.
     */
    public String logLevel;

    /**
     * Base URL to create new URLs.
     */
    public String baseUrl;

    /**
     * Directory with stored plugins.
     */
    public String pluginDirectory;

    /**
     * URL of executor-monitor instance.
     */
    public String executorMonitorUrl;

    /**
     * Create new configuration, use values from this instance
     * as defaults.
     */
    public Configuration merge(Configuration other) {
        Configuration result = new Configuration();

        result.httpPort = mergeProperty(
                httpPort, other.httpPort);
        result.dataDirectory = mergeProperty(
                dataDirectory, other.dataDirectory);
        result.logDirectory = mergeProperty(
                logDirectory, other.logDirectory);
        result.logLevel = mergeProperty(
                logLevel, other.logLevel);
        result.baseUrl = mergeProperty(
                baseUrl, other.baseUrl);
        result.pluginDirectory = mergeProperty(
                pluginDirectory, other.pluginDirectory);
        result.executorMonitorUrl = mergeProperty(
                executorMonitorUrl, other.executorMonitorUrl);

        return result;
    }

    private <T> T mergeProperty(T left, T right) {
        return right == null ? left : right;
    }

}
