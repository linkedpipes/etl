package com.linkedpipes.etl.executor.cli;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Configuration {

    /**
     * HTTP server port.
     */
    public Integer httpPort;

    /**
     * Directory where executor should store data.
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
     * OSGI working directory; used mostly as a cache.
     */
    public String osgiWorkingDirectory;

    /**
     * OSGI libraries to be loaded.
     */
    public String osgiLibrariesDirectory;

    /**
     * OSGI plugins to be loaded; mostly the components.
     */
    public String pluginsDirectory;

    /**
     * List of regular expressions. If component IRI match any of the
     * patterns and is used in execution the execution fail before
     * executing the component.
     */
    public List<String> bannedPluginIriPatterns = Collections.emptyList();

    /**
     * Create new configuration, use values from this instance
     * as defaults.
     *
     * See implementation for exceptions.
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
        result.osgiWorkingDirectory = mergeProperty(
                osgiWorkingDirectory, other.osgiWorkingDirectory);
        result.osgiLibrariesDirectory = mergeProperty(
                osgiLibrariesDirectory, other.osgiLibrariesDirectory);
        result.pluginsDirectory = mergeProperty(
                pluginsDirectory, other.pluginsDirectory);

        // Banning components is additive.
        result.bannedPluginIriPatterns = new ArrayList<>();
        result.bannedPluginIriPatterns.addAll(this.bannedPluginIriPatterns);
        result.bannedPluginIriPatterns.addAll(bannedPluginIriPatterns);

        return result;
    }

    private <T> T mergeProperty(T left, T right) {
        return right == null ? left : right;
    }

}
