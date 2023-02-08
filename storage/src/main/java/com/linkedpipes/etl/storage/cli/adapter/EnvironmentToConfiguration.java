package com.linkedpipes.etl.storage.cli.adapter;

import com.linkedpipes.etl.storage.cli.Configuration;

public class EnvironmentToConfiguration {

    public static Configuration updateConfiguration(
            Configuration defaults) {
        Configuration next = new Configuration();
        next.baseUrl = getEnv(
                "LP_ETL_DOMAIN");
        next.executorMonitorUrl = getEnv(
                "LP_ETL_MONITOR_URL");
        return defaults.merge(next);
    }

    private static String getEnv(String name) {
        return System.getenv(name);
    }

}
