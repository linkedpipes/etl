package com.linkedpipes.plugin.loader.scp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;

/**
 * Used library: http://www.jcraft.com/jsch/examples/ SCP protocol:
 * https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works
 *
 * This class is not thread save.
 */
class ScpClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ScpClient.class);

    private static final int DATA_AWAIT_SLEEP_MS = 1000;

    private static final int DATA_AWAIT_MAX_ITERATIONS = 10;

    private static final int COMMAND_WAIT_MS = 500;

    @FunctionalInterface
    interface ChanelConsumer {

        void accept(ChannelExec channel) throws Exception;

    }

    private final JSch jsch = new JSch();

    private final int timeOut;

    private Session session = null;

    private OutputStream remoteOutput;

    private InputStream remoteInput;

    public ScpClient(int timeOut) {
        this.timeOut = timeOut;
    }

    public void connect(
            String hostname, int port, String username, String password)
            throws Exception {
        LOG.debug("connect ...");
        this.session = jsch.getSession(username, hostname, port);
        this.session.setPassword(password);
        this.session.setTimeout(this.timeOut);
        configureSession();
        this.session.connect();
        LOG.debug("session.isConnected: {}", session.isConnected());
        LOG.debug("connect ... done");
    }

    private void configureSession() {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        this.session.setConfig(config);
    }

    public void createDirectory(String directory) throws Exception {
        LOG.info("createDirectory ...");
        withChannelExec((channel) -> {
            String command = this.createDirectoryCommand(directory);
            channel.setCommand(command);
            this.executeChannel(channel);
        });
        LOG.info("createDirectory ... done");
    }

    private String createDirectoryCommand(String directory) {
        return "[ -d " + directory + " ] || mkdir " + directory;
    }

    private void withChannelExec(ChanelConsumer consumer) throws Exception {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) this.session.openChannel("exec");
            consumer.accept(channel);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    private void executeChannel(ChannelExec channel) throws Exception {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        channel.setErrStream(errorStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        channel.setExtOutputStream(errorStream);
        //
        channel.connect();
        LOG.debug("Waiting for command to complete ...");
        this.waitTillCommandIsFinished(channel);
        LOG.debug("Reading response status ...");
        int status = channel.getExitStatus();
        LOG.info("Status code: {}", status);
        switch (status) {
            case -1: // Fail with no output )command not done yet?).
                throw new LpException("Action failed (-1).");
            case 0: // Ok
                return;
            case 1: // Failure
            case 2: // Critical failure
                LOG.info("Stderr: {}", errorStream.toString());
                LOG.info("Stdout: {}", outputStream.toString());
                throw new LpException(
                        "Action failed, see log for more information.");
            default:
                throw new LpException("Unexpected status: {}", status);
        }
    }

    private void waitTillCommandIsFinished(Channel channel) {
        while (!channel.isClosed()) {
            try {
                Thread.sleep(COMMAND_WAIT_MS);
            } catch (Exception e) {
                // No operation here.
            }
        }
    }

    public void clearDirectory(String directory) throws Exception {
        String command =
                "`[ -d " + directory + " ] && rm -r " + directory + "/* || :`";
        LOG.info("clearDirectory ... : {}", command);
        withChannelExec((channel) -> {
            channel.setCommand(command);
            executeChannel(channel);
        });
        LOG.info("clearDirectory ... done");
    }

    public void uploadDirectories(
            String directory, Collection<File> directories) throws Exception {
        LOG.info("uploadDirectories ...");
        String command = "scp -r -t -d " + directory;
        withChannelExec((channel) -> {
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            channel.setErrStream(errorStream);
            try (OutputStream remoteOutput = channel.getOutputStream();
                 InputStream remoteInput = channel.getInputStream()) {
                this.remoteOutput = remoteOutput;
                this.remoteInput = remoteInput;
                //
                channel.setCommand(command);
                channel.connect();
                this.checkResponseStream();
                for (File file : directories) {
                    this.sendDirectory(file);
                }
            }
        });
        LOG.info("uploadDirectories ... done");
    }

    private void sendDirectory(File sourceDirectory)
            throws IOException, LpException {
        for (File file : sourceDirectory.listFiles()) {
            if (file.isDirectory()) {
                this.sendDirectory(file, file.getName());
            }
            if (file.isFile()) {
                this.sendFile(file, file.getName());
            }
        }
    }

    private void sendDirectory(File sourceDirectory, String directoryName)
            throws IOException, LpException {
        LOG.debug("Sending directory: {} ... ", directoryName);
        // Send command.
        String command = "D0755 0 " + directoryName + "\n";
        this.remoteOutput.write(command.getBytes());
        this.remoteOutput.flush();
        this.checkResponseStream();
        // Scan for files.
        sendDirectory(sourceDirectory);
        this.remoteOutput.write("E\n".getBytes());
        this.remoteOutput.flush();
        this.checkResponseStream();
        LOG.debug("Sending directory: {} ... done", directoryName);
    }

    private void sendFile(File sourceFile, String fileName)
            throws IOException, LpException {
        LOG.debug("Sending file: {} ... ", fileName);
        if (fileName.indexOf('/') > 0) {
            throw new IllegalArgumentException("File name '" + fileName + "'");
        }
        // Send command for new file.
        final Long fileSize = sourceFile.length();
        String command = "C0644 " + fileSize + " " + fileName + "\n";
        this.remoteOutput.write(command.getBytes());
        this.remoteOutput.flush();
        this.checkResponseStream();
        // Copy file.
        try (InputStream sourceFileStream = new FileInputStream(sourceFile)) {
            IOUtils.copy(sourceFileStream, this.remoteOutput);
        }
        this.remoteOutput.flush();
        // Write '\0' as the end of file.
        this.remoteOutput.write(0);
        this.remoteOutput.flush();
        // Check status.
        this.checkResponseStream();
        LOG.debug("Sending file: {} ... done", fileName);
    }

    private void checkResponseStream() throws IOException, LpException {
        int response = this.remoteInput.read();
        switch (response) {
            case -1: // No response from server.
                throw new LpException("No response from server!");
            case 0: // Success.
                break;
            case 1:
                throw new LpException("Error: {}", this.readResponse());
            case 2:
                throw new LpException("Fatal error: {}", this.readResponse());
            default:
                throw new LpException("Invalid response: {}", response);
        }
    }

    private String readResponse() throws IOException {
        LOG.debug("readResponse ...");
        if (this.remoteInput == null) {
            return "";
        }
        this.waitForData(this.remoteInput);
        StringBuffer buffer = new StringBuffer();
        while (true) {
            int value = this.remoteInput.read();
            if (value == 0 || value == -1) {
                break;
            }
            buffer.append((char) value);
        }
        LOG.debug("readResponse ... done");
        return buffer.toString();
    }

    private void waitForData(InputStream stream) throws IOException {
        int waitCounter = 0;
        do {
            try {
                Thread.sleep(DATA_AWAIT_SLEEP_MS);
            } catch (Exception e) {
                // No operation here.
            }
            ++waitCounter;
            if (waitCounter > DATA_AWAIT_MAX_ITERATIONS) {
                throw new IOException("No data arrived in time: " +
                        (DATA_AWAIT_MAX_ITERATIONS * DATA_AWAIT_SLEEP_MS) +
                        " ms");
            }
        } while (stream.available() == 0);
    }

    @Override
    public void close() {
        if (this.session != null) {
            this.session.disconnect();
        }
    }

}
