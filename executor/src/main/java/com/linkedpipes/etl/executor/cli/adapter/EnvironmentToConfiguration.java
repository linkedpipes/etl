package com.linkedpipes.etl.executor.cli.adapter;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.cli.Configuration;

import java.util.Collections;

public class EnvironmentToConfiguration {

    public static Configuration updateConfiguration(
            Configuration defaults)
            throws ExecutorException {
        Configuration next = new Configuration();
        next.httpPort = getEnvInteger(
                "LP_ETL_EXECUTOR_PORT");
        next.dataDirectory = getEnv(
                "LP_ETL_EXECUTOR_DATA");
        next.osgiWorkingDirectory =getEnv(
                "LP_ETL_EXECUTOR_OSGI") ;
        next.osgiLibrariesDirectory =getEnv(
                "LP_ETL_EXECUTOR_LIBRARIES") ;
        next.pluginsDirectory =getEnv(
                "LP_ETL_STORAGE_PLUGINS") ;
        return defaults.merge(next);
    }

    private static String getEnv(String name) {
        return System.getenv(name);
    }

    private static Integer getEnvInteger(String name) throws ExecutorException {
        String value = getEnv(name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            throw new ExecutorException(
                    "Invalid configuration property: '{}'", name);
        }
    }

}
