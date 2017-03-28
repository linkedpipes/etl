package com.linkedpipes.plugin.extractor.ftpfiles;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class FtpFiles implements Component, SequentialExecution {

    class ProtocolLogger implements ProtocolCommandListener {

        @Override
        public void protocolCommandSent(ProtocolCommandEvent event) {
            LOG.debug("sent: {}, {} -> {}", event.getCommand(),
                    event.getMessage(), event.getReplyCode());
        }

        @Override
        public void protocolReplyReceived(ProtocolCommandEvent event) {
            if (isNegativeReply(event)) {
                LOG.error("received: {} -> {}",
                        event.getMessage(), event.getReplyCode());
            } else {
                LOG.debug("received: {} -> {}",
                        event.getMessage(), event.getReplyCode());
            }
        }

        private boolean isNegativeReply(ProtocolCommandEvent event) {
            return event.getReplyCode() >= 400;
        }

    }

    private static final Logger LOG
            = LoggerFactory.getLogger(FtpFiles.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public FtpFilesConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private ProtocolLogger protocolLogger = new ProtocolLogger();

    @Override
    public void execute() throws LpException {
        progressReport.start(configuration.getReferences().size());
        for (FtpFilesConfiguration.Reference reference
                : configuration.getReferences()) {
            // Prepare URI and file.
            final URL url;
            try {
                url = new URL(reference.getUri());
            } catch (MalformedURLException ex) {
                LOG.error("Wrong URI format: {}", reference.getUri(), ex);
                continue;
            }
            final File file = output.createFile(
                    reference.getFileName());
            // Download.
            try {
                downloadFile(url, file);
            } catch (IOException ex) {
                throw exceptionFactory.failure("Download failed.", ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void downloadFile(URL sourceUri, File file)
            throws IOException, LpException {
        final String host = sourceUri.getHost();
        final String filePath = sourceUri.getPath();
        LOG.debug("Host: {} Path: {} -> {}", host, filePath, file.getName());
        // Connect to remote host.
        final FTPClient client = new FTPClient();
        if (sourceUri.getPort() == -1) {
            client.connect(host);
        } else {
            client.connect(host, sourceUri.getPort());
        }

        // Can be used to track progress.
        client.setCopyStreamListener(new ProgressPrinter());

        // Debug.
        client.addProtocolCommandListener(protocolLogger);

        // Set time out.
        client.setDataTimeout(5000);
        client.setControlKeepAliveTimeout(configuration.getKeepAliveControl());

        int reply = client.getReplyCode();
        LOG.debug("Connect reply: {}, {}", reply, client.getReplyString());
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw exceptionFactory.failure("Server refused connection.");
        }

        // For now support only anonymous.
        if (!client.login("anonymous", "")) {
            client.logout();
            client.disconnect();
            throw exceptionFactory.failure(
                    "Can't login as 'anonymous' with no password.");
        }
        reply = client.getReplyCode();
        LOG.debug("Connect reply: {}, {}", reply, client.getReplyString());
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw exceptionFactory.failure("Server refused the connection.");
        }

        // From documentation:
        //      currently calling any connect method will reset
        //      the mode to ACTIVE_LOCAL_DATA_CONNECTION_MODE.
        if (configuration.isUsePassiveMode()) {
            LOG.debug("Using passive mode.");
            client.enterLocalPassiveMode();
        }

        // From documentation:
        //      currently calling any connect method will reset
        //      the type to FTP.ASCII_FILE_TYPE.
        if (configuration.isUseBinaryMode()) {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
        }

        LOG.debug("Downloading ...");
        try (FileOutputStream output = new FileOutputStream(file)) {
            if (!client.retrieveFile("/" + filePath, output)) {
                throw exceptionFactory.failure(
                        "Failed to download file. See logs for more detail.");
            }
            LOG.debug("Downloading ... flush");
            output.flush();
        }
        LOG.debug("Downloading ... done");

        client.logout();
        client.disconnect();
    }

}
