package com.linkedpipes.etl.executor.monitor.cli;

public class Configuration {

    /**
     * HTTP server port.
     */
    public Integer httpPort;

    /**
     * Directory with executor's data.
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
     * URL of executor instance.
     */
    public String executorUrl;

    /**
     * Base URL to create new URLs.
     */
    public String baseUrl;

    /**
     * Used in HTTP debug as a prefix to construct URL.
     */
    public String publicWorkingDataUrlPrefix;

    /**
     * If not null, set the number of re-execution for dangling
     * executions.
     */
    public Integer danglingRetryLimit;

    /**
     * Limit number of stored execution for stored pipeline.
     * Oldest execution is deleted when the limit is exceeded.
     */
    public Integer historyLimit;

    /**
     * Delete execution older than given time.
     */
    public Integer historyHourLimit;

    /**
     * Slack integration.
     * Webhook to send information about finished execution.
     */
    public String slackFinishedWebhook;

    /**
     * Slack integration.
     * Webhook to send information about failed executions.
     */
    public String slackErrorWebhook;

    /**
     * FTP server.
     * Port for FTP commands.
     */
    public Integer ftpCommandPort;

    /**
     * FTP server.
     * Start of data port range.
     */
    public Integer ftpDataPortsStart;

    /**
     * FTP server.
     * End of data port range.
     */
    public Integer ftpDataPortsEnd;

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
        result.executorUrl = mergeProperty(
                executorUrl, other.executorUrl);
        result.baseUrl = mergeProperty(
                baseUrl, other.baseUrl);
        result.publicWorkingDataUrlPrefix = mergeProperty(
                publicWorkingDataUrlPrefix, other.publicWorkingDataUrlPrefix);

        result.danglingRetryLimit = mergeProperty(
                danglingRetryLimit, other.danglingRetryLimit);

        result.historyLimit = mergeProperty(
                historyLimit, other.historyLimit);
        result.historyHourLimit = mergeProperty(
                historyHourLimit, other.historyHourLimit);

        result.slackFinishedWebhook = mergeProperty(
                slackFinishedWebhook, other.slackFinishedWebhook);
        result.slackErrorWebhook = mergeProperty(
                slackErrorWebhook, other.slackErrorWebhook);

        result.ftpCommandPort = mergeProperty(
                ftpCommandPort, other.ftpCommandPort);
        result.ftpDataPortsStart = mergeProperty(
                ftpDataPortsStart, other.ftpDataPortsStart);
        result.ftpDataPortsEnd = mergeProperty(
                ftpDataPortsEnd, other.ftpDataPortsEnd);

        return result;
    }

    private <T> T mergeProperty(T left, T right) {
        return right == null ? left : right;
    }

}
