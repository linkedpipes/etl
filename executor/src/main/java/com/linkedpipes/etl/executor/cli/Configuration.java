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

    public Configuration merge(
            Integer httpPort,
            String dataDirectory,
            String logDirectory,
            String logLevel,
            String osgiWorkingDirectory,
            String osgiLibrariesDirectory,
            String pluginsDirectory,
            List<String> bannedPluginIriPatterns) {
        return new Configuration(
                httpPort == null ?
                        this.httpPort :
                        httpPort,
                dataDirectory == null ?
                        this.dataDirectory :
                        dataDirectory,
                logDirectory == null ?
                        this.logDirectory :
                        logDirectory,
                logLevel == null ?
                        this.logLevel :
                        logLevel,
                osgiWorkingDirectory == null ?
                        this.osgiWorkingDirectory :
                        osgiWorkingDirectory,
                osgiLibrariesDirectory == null ?
                        this.osgiLibrariesDirectory :
                        osgiLibrariesDirectory,
                pluginsDirectory == null ?
                        this.pluginsDirectory :
                        pluginsDirectory,
                bannedPluginIriPatterns == null ?
                        this.bannedPluginIriPatterns :
                        bannedPluginIriPatterns
        );
    }

}
