package com.linkedpipes.plugin.loader.ftpfiles;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FtpFilesLoader implements Component, SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(FtpFilesLoader.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit input;

    @Component.Configuration
    public FtpFilesLoaderConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private FTPClient ftpClient;

    @Override
    public void execute() throws LpException {
        try {
            initializeClient();
            uploadFiles();
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't upload files.", ex);
        } finally {
            closeClient();
        }
    }

    private void initializeClient() throws IOException {
        ftpClient = new FTPClient();
        ftpClient.connect(configuration.getServer(), configuration.getPort());
        ftpClient.login(configuration.getUser(), configuration.getPassword());
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    }

    private void uploadFiles() throws IOException, LpException {
        progressReport.start(input.size());
        for (FilesDataUnit.Entry entry : input) {
            LOG.info("Uploading : {}", entry.getFileName());
            uploadFile(entry.toFile(), entry.getFileName());
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void uploadFile(File file, String remoteName)
            throws IOException, LpException {
        final String remoteFullName = getRemoteFullPath(remoteName);
        checkAndCreateDirectory(remoteFullName);
        try (InputStream inputStream = new FileInputStream(file)) {
            if (!ftpClient.storeFile(remoteFullName, inputStream)) {
                throw exceptionFactory.failure(
                        "Can't upload file. Response code: {} message: {}",
                        ftpClient.getReplyCode(), ftpClient.getReplyString());
            }
            LOG.info("\t {} : {}",
                    ftpClient.getReplyCode(), ftpClient.getReplyString());
        }
    }

    private String getRemoteFullPath(String remoteName) {
        return "/" + configuration.getDirectory() + "/" + remoteName;
    }

    private void checkAndCreateDirectory(String path)
            throws IOException, LpException {
        // We use changeWorkingDirectory to check if a directory exists.
        final String[] splitPath = path.replace("\\", "/").split("/");
        String subPath = "/";
        for (int index = 0; index < splitPath.length - 1; ++index) {
            subPath += splitPath[index] + "/";
            if (!ftpClient.changeWorkingDirectory(subPath)) {
                createDirectory(subPath);
            }
        }
        if (!ftpClient.changeWorkingDirectory(subPath)) {
            throw exceptionFactory
                    .failure("Failed to create directories for: {}", subPath);
        }
        if (!ftpClient.changeWorkingDirectory("/")) {
            throw exceptionFactory
                    .failure("Can't change working directory to root.");
        }
    }

    private void createDirectory(String path) throws IOException, LpException {
        if (!ftpClient.makeDirectory(path)) {
            throw exceptionFactory.failure(
                    "Can't create directory: {}", path);
        }
    }

    private void closeClient() {
        if (ftpClient != null) {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                LOG.error("Can't close FTP connection.", ex);
            }
        }
    }

}
