package com.linkedpipes.plugin.loader.scp;

import com.jcraft.jsch.*;
import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Used library: http://www.jcraft.com/jsch/examples/ SCP protocol:
 * https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works
 *
 * JSCH also supports SFTP, example on the project site and on
 * http://stackoverflow.com/questions/2346764/java-sftp-transfer-library
 */
public final class LoaderScp implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(LoaderScp.class);

    @Component.OutputPort(iri = "FilesInput")
    public FilesDataUnit input;

    @Component.Configuration
    public LoaderScpConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final String user = configuration.getUserName();
        final String password = configuration.getPassword();
        final String host = configuration.getHost();
        final int port = configuration.getPort();
        final String targetFile = configuration.getTargetDirectory();
        if (user == null || user.isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    LoaderScpVocabulary.HAS_USERNAME);
        }
        if (password == null || password.isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    LoaderScpVocabulary.HAS_PASSWORD);
        }
        if (host == null || host.isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    LoaderScpVocabulary.HAS_HOST);
        }
        if (targetFile == null || targetFile.isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    LoaderScpVocabulary.HAS_TARGET_DIRECTORY);
        }
        //
        final JSch jsch = new JSch();
        // Create session.
        final Session session;
        try {
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
        } catch (JSchException ex) {
            throw exceptionFactory.failure("Can't create session.", ex);
        }
        // Enable connection to machines with unknown host
        // key - this is potential security risk!
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        try {
            session.connect();
        } catch (JSchException ex) {
            throw exceptionFactory.failure("Can't connect to host", ex);
        }
        try {
            upload(session, targetFile);
        } finally {
            session.disconnect();
        }
    }

    public void upload(Session session, String targetFile)
            throws LpException {

        if (configuration.isCreateDirectory()) {
            try {
                secureCreateDirectory(session, targetFile);
            } catch (JSchException | SftpException | IOException ex) {
                throw exceptionFactory.failure("Can't create directory.", ex);
            }
        }

        if (configuration.isClearDirectory()) {
            try {
                deleteDirectory(session, targetFile);
            } catch (JSchException | SftpException | IOException ex) {
                throw exceptionFactory
                        .failure("Can't clear upload directory.", ex);
            }
        }

        // Execute 'scp -t targetFile' on remote machine and get related streams.
        final Channel channel;
        try {
            channel = session.openChannel("exec");
        } catch (JSchException ex) {
            throw exceptionFactory.failure("Can't create session.", ex);
        }
        // File transfer.
        // -r - enable copy of empty directory
        //  echo D0755 0 testdir;
        //  echo E
        // -d - means directory transfer
        ((ChannelExec) channel).setCommand("scp -r -t -d " + targetFile);
        try (OutputStream remoteOut = channel.getOutputStream();
             InputStream remoteIn = channel.getInputStream()) {
            channel.connect();
            responseCheck(remoteIn);
            // Send content of files data unit.
            for (File rootDirectory : input.getReadDirectories()) {
                sendDirectoryContent(remoteOut, remoteIn, rootDirectory);
            }
        } catch (IOException | JSchException | LpException ex) {
            throw exceptionFactory.failure(
                    "Can't upload data!", ex);
        } finally {
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private void deleteDirectory(Session session,
            String targetPath) throws JSchException, IOException,
            SftpException, LpException {
        LOG.info("deleteDirectory ...");
        final Channel channel = session.openChannel("exec");
        // We just execute given command.
        ((ChannelExec) channel).setCommand("rm -r " + targetPath + "/*");
        final InputStream remoteIn = channel.getExtInputStream();
        channel.connect();
        LOG.info("\tWaiting for response!");
        final String responseLine = readResponseLine(remoteIn);
        LOG.info("\tResponse status: {} message: {}",
                channel.getExitStatus(), responseLine);
        channel.disconnect();
        if (channel.getExitStatus() != 0) {
            throw exceptionFactory.failure("Can't clear directory");
        }
        LOG.info("deleteDirectory ... done");
    }

    /**
     * Experimental function create target directory.
     *
     * @param session
     * @param targetPath
     */
    private void secureCreateDirectory(Session session,
            String targetPath) throws JSchException, IOException,
            SftpException, LpException {
        LOG.info("secureCreateDirectory ...");
        final Channel channel = session.openChannel("exec");
        // We just execute given command.
        ((ChannelExec) channel).setCommand("mkdir " + targetPath);
        final InputStream remoteIn = channel.getExtInputStream();
        channel.connect();
        LOG.info("\tWaiting for response!");
        final String responseLine = readResponseLine(remoteIn);
        LOG.info("\tResponse status: {} message: {}",
                channel.getExitStatus(), responseLine);
        channel.disconnect();
        checkMakeDirResponse(responseLine, channel.getExitStatus());
        LOG.info("secureCreateDirectory ... done");
    }

    private void checkMakeDirResponse(String responseLine,
            int status) throws LpException {
        if (status == 0) {
            return;
        }
        if (responseLine.contains("File exists")) {
            return;
        }
        throw exceptionFactory.failure("Can't create directory");
    }

    /**
     * Send content of given directory.
     *
     * @param remoteOut
     * @param remoteIn
     * @param sourceDirectory
     */
    private void sendDirectoryContent(OutputStream remoteOut,
            InputStream remoteIn, File sourceDirectory)
            throws IOException, LpException {
        // Scan for files.
        for (final File file : sourceDirectory.listFiles()) {
            if (file.isDirectory()) {
                sendDirectory(remoteOut, remoteIn, file, file.getName());
            }
            if (file.isFile()) {
                sendFile(remoteOut, remoteIn, file, file.getName());
            }
        }
    }

    /**
     * Send content of given directory under remote directory of given name.
     *
     * @param remoteOut
     * @param remoteIn
     * @param sourceDirectory
     * @param directoryName
     */
    private void sendDirectory(OutputStream remoteOut,
            InputStream remoteIn, File sourceDirectory, String directoryName)
            throws IOException, LpException {
        LOG.debug("Sending directory: {} ... ", directoryName);
        // Send command.
        String command = "D0755 0 " + directoryName + "\n";
        remoteOut.write(command.getBytes());
        remoteOut.flush();
        responseCheck(remoteIn);
        // Scan for files.
        for (final File file : sourceDirectory.listFiles()) {
            if (file.isDirectory()) {
                sendDirectory(remoteOut, remoteIn, file, file.getName());
            }
            if (file.isFile()) {
                sendFile(remoteOut, remoteIn, file, file.getName());
            }
        }
        remoteOut.write("E\n".getBytes());
        remoteOut.flush();
        responseCheck(remoteIn);
        LOG.debug("Sending directory: {} ... done", directoryName);
    }

    /**
     * @param remoteOut
     * @param remoteIn
     * @param sourceFile
     * @param fileName Must not include '/'.
     */
    private void sendFile(OutputStream remoteOut, InputStream remoteIn,
            File sourceFile, String fileName) throws IOException, LpException {
        LOG.debug("Sending file: {} ... ", fileName);
        if (fileName.indexOf('/') > 0) {
            throw new IllegalArgumentException("File name '" + fileName + "'");
        }
        // Send command for new file.
        final Long fileSize = sourceFile.length();
        String command = "C0644 " + fileSize + " " + fileName + "\n";
        remoteOut.write(command.getBytes());
        remoteOut.flush();
        responseCheck(remoteIn);
        // Copy file.
        try (FileInputStream sourceFileStream
                     = new FileInputStream(sourceFile)) {
            IOUtils.copy(sourceFileStream, remoteOut);
        }
        remoteOut.flush();
        // Write '\0' as the end of file.
        remoteOut.write(0);
        remoteOut.flush();
        // Check status.
        responseCheck(remoteIn);
        LOG.debug("Sending file: {} ... done", fileName);
    }

    /**
     * Check response from SCP server. Throws exception in case of error.
     *
     * @param stream
     */
    private void responseCheck(InputStream stream)
            throws IOException, LpException {
        final int response = stream.read();
        switch (response) {
            case -1: // No response from server.
                throw exceptionFactory.failure(
                        "No response from server!");
            case 0: // Success.
                break;
            case 1:
                throw exceptionFactory.failure("Error: {}",
                        readResponseLine(stream));
            case 2:
                throw exceptionFactory.failure("Fatal error: {}",
                        readResponseLine(stream));
            default:
                throw exceptionFactory.failure(
                        "Invalid reponse: {}", response);
        }
    }

    /**
     * Read line ended with new line symbol from given stream.
     *
     * @param stream
     * @return
     */
    private static String readResponseLine(InputStream stream)
            throws IOException {
        if (stream == null) {
            return "";
        }
        final StringBuffer buffer = new StringBuffer();
        int value;
        while (true) {
            value = stream.read();
            if (value == '\n' || value == 0 || value == -1) {
                break;
            }
            buffer.append((char) value);
        }
        return buffer.toString();
    }

}
