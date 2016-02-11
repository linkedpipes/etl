package com.linkedpipes.executor;

import com.linkedpipes.commons.code.configuration.boundary.AbstractConfiguration;
import java.io.File;
import org.springframework.stereotype.Service;

/**
 *
 * @author Škoda Petr
 */
@Service
public class Configuration  extends AbstractConfiguration {

    private int webServerPort;

    private String logDirectoryPath;

    private String logCoreFilter;

    private String osgiLibDirectoryPath;

    private String executionPrefix;

    private String osgiStorageDirectory;

    @Override
    protected void loadProperties() {
        webServerPort = getPropertyInteger("executor.webserver.port");
        logDirectoryPath = getProperty("executor.log.directory", validateDirectory());
        logCoreFilter = getProperty("executor.log.core.level");
        osgiLibDirectoryPath = getProperty("executor.osgi.lib.directory", validateDirectory());
        executionPrefix = getProperty("executor.execution.uriPrefix");
        osgiStorageDirectory = getProperty("executor.osgi.working.directory", validateDirectory());
    }

    public File getLogDirectory() {
        final File logDirectory = new File(logDirectoryPath);
        logDirectory.mkdirs();
        return logDirectory;
    }

    public int getWebServerPort() {
        return webServerPort;
    }

    public String getLogCoreFilter() {
        return logCoreFilter;
    }

    public File getOsgiLibDirectory() {
        return new File(osgiLibDirectoryPath);
    }

    public String getExecutionPrefix() {
        return executionPrefix;
    }

    public String getOsgiStorageDirectory() {
        return osgiStorageDirectory;
    }

}
