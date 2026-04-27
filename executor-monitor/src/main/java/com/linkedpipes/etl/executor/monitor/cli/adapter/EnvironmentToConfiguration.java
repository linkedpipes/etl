package com.linkedpipes.etl.executor.monitor.cli.adapter;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.cli.Configuration;

public class EnvironmentToConfiguration {

    public static Configuration updateConfiguration(
            Configuration defaults)
            throws MonitorException {
        Configuration next = new Configuration();
        next.baseUrl = getEnv(
                "LP_ETL_DOMAIN");
        next.executorUrl = getEnv(
                "LP_ETL_EXECUTOR_URL");
        next.danglingRetryLimit =  getEnvInteger(
                "LP_ETL_EXECUTION_RETRY_LIMIT");
        next.historyLimit =  getEnvInteger(
                "LP_ETL_EXECUTION_HISTORY_COUNT_LIMIT");
        next.historyHourLimit =  getEnvInteger(
                "LP_ETL_EXECUTION_HISTORY_HOUR_LIMIT");
        next.slackFinishedWebhook= getEnv(
                "LP_ETL_SLACK_FINISHED_WEBHOOK");
        next.slackErrorWebhook= getEnv(
                "LP_ETL_SLACK_ERROR_WEBHOOK");
        return defaults.merge(next);
    }

    private static String getEnv(String name) {
        return System.getenv(name);
    }

    private static Integer getEnvInteger(String name) throws MonitorException {
        String value = getEnv(name);
        if (value == null || value.isBlank()) {
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
