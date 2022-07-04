package com.linkedpipes.etl.executor.cli;

import java.util.Collections;
import java.util.List;

public record Configuration(
        /*
         * HTTP server port.
         */
        Integer httpPort,
        /*
         * Directory where executor should store data.
         */
        String dataDirectory,
        /*
         * Directory where to store logs.
         */
        String logDirectory,
        /*
         * Log level to employ.
         */
        String logLevel,
        /*
         * OSGI working directory, used mostly as a cache.
         */
        String osgiWorkingDirectory,
        /*
         * OSGI libraries to be loaded.
         */
        String osgiLibrariesDirectory,
        /*
         * OSGI plugins to be loaded, mostly the components.
         */
        String pluginsDirectory,
        /*
         * URL of the storage component.
         */
        String storageUrl,
        /*
         * List of regular expressions. If component IRI match any of the
         * patterns and is used in execution the execution fail before
         * executing the component.
         */
        List<String> bannedPluginIriPatterns
) {

    public Configuration {
        bannedPluginIriPatterns = Collections.unmodifiableList(
                bannedPluginIriPatterns);
    }

    public Configuration(
            Configuration configuration,
            Integer httpPort,
            String dataDirectory,
            String logDirectory,
            String logLevel,
            String osgiWorkingDirectory,
            String osgiLibrariesDirectory,
            String pluginsDirectory,
            String storageUrl,
            List<String> bannedPluginIriPatterns) {
        this(
                httpPort == null ?
                        configuration.httpPort :
                        httpPort,
                dataDirectory == null ?
                        configuration.dataDirectory :
                        dataDirectory,
                logDirectory == null ?
                        configuration.logDirectory :
                        logDirectory,
                logLevel == null ?
                        configuration.logLevel :
                        logLevel,
                osgiWorkingDirectory == null ?
                        configuration.osgiWorkingDirectory :
                        osgiWorkingDirectory,
                osgiLibrariesDirectory == null ?
                        configuration.osgiLibrariesDirectory :
                        osgiLibrariesDirectory,
                pluginsDirectory == null ?
                        configuration.pluginsDirectory :
                        pluginsDirectory,
                storageUrl == null ?
                        configuration.storageUrl :
                        storageUrl,
                bannedPluginIriPatterns == null ?
                        configuration.bannedPluginIriPatterns :
                        bannedPluginIriPatterns
        );
    }

}
