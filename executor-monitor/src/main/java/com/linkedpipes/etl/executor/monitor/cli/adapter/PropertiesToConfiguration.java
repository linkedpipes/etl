package com.linkedpipes.etl.executor.monitor.cli.adapter;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.cli.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertiesToConfiguration {

    private final Properties properties;

    public static Configuration updateConfiguration(
            Configuration defaults, File file)
            throws MonitorException {
        PropertiesToConfiguration instance = new PropertiesToConfiguration(
                loadProperties(file));
        //
        Configuration next = new Configuration();
        next.httpPort = instance.getInteger(
                "executor-monitor.webserver.port");
        next.executorUrl = instance.getString(
                "executor.webserver.uri");
        next.dataDirectory = instance.getString(
                "executor.execution.working_directory");
        next.logDirectory = instance.getString(
                "executor-monitor.log.directory");
        next.logLevel = instance.getString(
                "executor-monitor.log.core.level");
        next.baseUrl = instance.getString(
                "domain.uri");
        next.publicWorkingDataUrlPrefix = instance.getString(
                "executor-monitor.public_working_data_url_prefix");

        next.danglingRetryLimit = instance.getInteger(
                "executor-monitor.retry_limit");

        next.historyLimit = instance.getInteger(
                "executor-monitor.history_limit");
        next.historyHourLimit = instance.getInteger(
                "executor-monitor.history_hour_limit");

        next.slackFinishedWebhook = instance.getString(
                "executor-monitor.slack_finished_executions_webhook");
        next.slackErrorWebhook = instance.getString(
                "executor-monitor.slack_error_webhook");

        next.ftpCommandPort = instance.getInteger(
                "executor-monitor.ftp.command_port");
        next.ftpDataPortsStart = instance.getInteger(
                "executor-monitor.ftp.data_ports_interval.start");
        next.ftpDataPortsEnd = instance.getInteger(
                "executor-monitor.ftp.data_ports_interval.end");
        return defaults.merge(next);
    }

    private static Properties loadProperties(File file)
            throws MonitorException {
        Properties properties = new Properties();
        try (var stream = new FileInputStream(file);
             var reader = new InputStreamReader(stream,
                     StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException ex) {
            throw new MonitorException("Can't load configuration file.", ex);
        }
        return properties;
    }

    private PropertiesToConfiguration(Properties properties) {
        this.properties = properties;
    }

    private String getString(String name) throws MonitorException {
        try {
            return properties.getProperty(name);
        } catch (RuntimeException ex) {
            throw new MonitorException(
                    "Invalid configuration property: '{}'", name, ex);
        }
    }

    private Integer getInteger(String name) throws MonitorException {
        String value = getString(name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            throw new MonitorException(
                    "Invalid configuration property: '{}'", name);
        }
    }

}
