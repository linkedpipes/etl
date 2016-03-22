package com.linkedpipes.etl.executor.monitor;

import com.linkedpipes.commons.code.configuration.boundary.AbstractConfiguration;
import java.io.File;

import org.springframework.stereotype.Service;

/**
 *
 * @author Škoda Petr
 */
@Service
public class Configuration extends AbstractConfiguration {

    private String workingDirectoryPath;

    private int webServerPort;

    private String logDirectoryPath;

    private String logFilter;

    private String executorUri;

    private String fusekiPath = null;

    private String externalWorkingDirectoryPath;

    private int ftpServerPort;

    private int processPortStart;

    private int processPortEnd;

    private String executionPrefix;

    @Override
    protected void loadProperties() {
        executorUri = getProperty("executor.webserver.uri", validateUri());
        workingDirectoryPath = getProperty("executor.execution.working_directory", validateDirectory());
        //
        webServerPort = getPropertyInteger("executor-monitor.webserver.port");
        logDirectoryPath = getProperty("executor-monitor.log.directory", validateDirectory());
        logFilter = getProperty("executor-monitor.log.core.level");
        fusekiPath = getProperty("external.fuseki.path");
        externalWorkingDirectoryPath = getProperty("external.working");
        ftpServerPort = getPropertyInteger("executor-monitor.ftp.port");
        processPortStart = getPropertyInteger("external.port.start");
        processPortEnd = getPropertyInteger("external.port.end");
        executionPrefix = getProperty("executor.execution.uriPrefix");
    }

    public File getWorkingDirectory() {
        final File workingDirectory = new File(workingDirectoryPath + File.separator + "data");
        workingDirectory.mkdirs();
        return workingDirectory;
    }

    public File getUploadDirectory() {
        final File uploadDirectory = new File(workingDirectoryPath + File.separator + "upload");
        uploadDirectory.mkdirs();
        return uploadDirectory;
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
        return logFilter;
    }

    public String getExecutorUri() {
        return executorUri;
    }

    public File getFusekiPath() {
        return new File(fusekiPath);
    }

    public File getExternalWorkingDirectoryPath() {
        final File result = new File(externalWorkingDirectoryPath);
        result.mkdirs();
        return result;
    }

    public int getFtpServerPort() {
        return ftpServerPort;
    }

    public int getProcessPortStart() {
        return processPortStart;
    }

    public int getProcessPortEnd() {
        return processPortEnd;
    }

    public String getExecutionPrefix() {
        return executionPrefix;
    }

}
