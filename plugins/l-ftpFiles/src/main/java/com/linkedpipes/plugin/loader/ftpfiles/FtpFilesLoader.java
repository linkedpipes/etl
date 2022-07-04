package com.linkedpipes.plugin.loader.ftpfiles;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class FtpFilesLoader implements Component, SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(FtpFilesLoader.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

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
            throw new LpException("Can't upload files.", ex);
        } finally {
            closeClient();
        }
    }

    private void initializeClient() throws IOException {
        ftpClient = new FTPClient();

        ftpClient.addProtocolCommandListener(new ProtocolCommandListener() {
            @Override
            public void protocolCommandSent(ProtocolCommandEvent event) {
                LOG.debug("command sent:\n\t{}\n\t{}\n\t{}",
                        event.getCommand(), event.getMessage(),
                        event.getReplyCode());
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent event) {
                LOG.debug("command received:\n\t{}\n\t{}\n\t{}",
                        event.getCommand(), event.getMessage(),
                        event.getReplyCode());
            }
        });

        ftpClient.connect(configuration.getServer(), configuration.getPort());
        ftpClient.login(configuration.getUser(), configuration.getPassword());
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
    }

    private void uploadFiles() throws LpException {
        if (input.size() == 0) {
            return;
        }
        progressReport.start(input.size());
        Iterator<FilesDataUnit.Entry> iterator = input.iterator();
        FilesDataUnit.Entry entry = iterator.next();
        int failCounter = 0;
        while (true) {
            LOG.info("Uploading : {}", entry.getFileName());
            try {
                uploadFile(entry.toFile(), entry.getFileName());
                progressReport.entryProcessed();
                failCounter = 0;
            } catch (Exception ex) {
                failCounter++;
                LOG.warn("Upload failed.", ex);
                if (failCounter >= configuration.getRetryCount()) {
                    throw new LpException("Download failed.");
                }
                // Reset connection and try to download the same entry again.
                LOG.debug("Trying again ...");
                restartFtpClient();
                continue;
            }
            // We have downloaded the file, move to next entry.
            if (iterator.hasNext()) {
                entry = iterator.next();
            } else {
                break;
            }
        }
        progressReport.done();
    }

    private void uploadFile(File file, String remoteName)
            throws IOException, LpException {
        String remoteFullName = getRemoteFullPath(remoteName);
        checkAndCreateDirectory(remoteFullName);
        try (InputStream inputStream = new FileInputStream(file)) {
            if (!ftpClient.storeFile(remoteFullName, inputStream)) {
                throw new LpException(
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
            throw new LpException(
                    "Failed to create directories for: {}", subPath);
        }
        if (!ftpClient.changeWorkingDirectory("/")) {
            throw new LpException(
                    "Can't change working directory to root.");
        }
    }

    private void createDirectory(String path) throws IOException, LpException {
        if (!ftpClient.makeDirectory(path)) {
            throw new LpException(
                    "Can't create directory: {}", path);
        }
    }

    private void restartFtpClient() throws LpException {
        closeClient();
        try {
            initializeClient();
        } catch (IOException ex) {
            throw new LpException("Can't reconnect to server.", ex);
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
