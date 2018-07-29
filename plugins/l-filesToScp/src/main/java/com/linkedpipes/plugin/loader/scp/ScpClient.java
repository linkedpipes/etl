package com.linkedpipes.plugin.loader.scp;

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
 */
class ScpClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ScpClient.class);

    private static final int DATA_AWAIT_SLEEP_MS = 1000;

    public static final int DATA_AWAIT_MAX_ITERATIONS = 10;

    @FunctionalInterface
    interface ChanelConsumer {

        void accept(ChannelExec channel) throws Exception;

    }

    private final JSch jsch = new JSch();

    private Session session = null;

    private int timeOut;

    public ScpClient(int timeOut) {
        this.timeOut = timeOut;
    }

    public void connect(
            String hostname, int port, String username, String password)
            throws Exception {
        LOG.debug("connect ...");
        this.session = jsch.getSession(username, hostname, port);
        this.session.setServerAliveInterval(this.timeOut);
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
            channel.setCommand(this.createDirectoryCommand(directory));
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
        channel.connect();
        LOG.debug("Reading response status ...");
        int status = channel.getExitStatus();
        LOG.debug("Reading response status ... done ({})", status);
        switch (status) {
            case -1: // Fail with no output
                throw new LpException("Action failed (-1).");
            case 0: // Ok
                return;
            case 1: // Failure
            case 2: // Critical failure
                this.handleError(status, channel);
            default:
                throw new LpException("Unexpected status: {}", status);
        }
    }

    private void handleError(int status, ChannelExec channel)
            throws IOException, LpException {
        // ExtInput also includes err stream.
        InputStream inputStream = channel.getExtInputStream();
        String response;
        try {
            response = readResponse(inputStream);
        } catch (IOException ex) {
            response = "";
            LOG.warn("Can't read server response.", ex);
        }
        LOG.error("Action failed, status: '{}' output: {}", status, response);
        throw new LpException("Action failed, see log for more information.");
    }

    private String readResponse(InputStream stream) throws IOException {
        LOG.debug("readResponse ...");
        if (stream == null) {
            return "";
        }
        this.waitForData(stream);
        StringBuffer buffer = new StringBuffer();
        while (true) {
            int value = stream.read();
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


    public void clearDirectory(String directory) throws Exception {
        String command = "[ $(ls -A " + directory + " ) ] && rm -r " +
                directory + "/* || :";
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
            try (OutputStream remoteOut = channel.getOutputStream();
                 InputStream remoteIn = channel.getInputStream()) {
                //
                channel.setCommand(command);
                channel.connect();
                checkResponseStream(remoteIn);
                for (File file : directories) {
                    sendDirectory(remoteOut, remoteIn, file);
                }
            }
        });
        LOG.info("uploadDirectories ... done");
    }

    private void sendDirectory(
            OutputStream remoteOut, InputStream remoteIn, File sourceDirectory)
            throws IOException, LpException {
        for (File file : sourceDirectory.listFiles()) {
            if (file.isDirectory()) {
                sendDirectory(remoteOut, remoteIn, file, file.getName());
            }
            if (file.isFile()) {
                sendFile(remoteOut, remoteIn, file, file.getName());
            }
        }
    }

    private void sendDirectory(
            OutputStream remoteOut, InputStream remoteIn,
            File sourceDirectory, String directoryName)
            throws IOException, LpException {
        LOG.debug("Sending directory: {} ... ", directoryName);
        // Send command.
        String command = "D0755 0 " + directoryName + "\n";
        remoteOut.write(command.getBytes());
        remoteOut.flush();
        checkResponseStream(remoteIn);
        // Scan for files.
        sendDirectory(remoteOut, remoteIn, sourceDirectory);
        remoteOut.write("E\n".getBytes());
        remoteOut.flush();
        checkResponseStream(remoteIn);
        LOG.debug("Sending directory: {} ... done", directoryName);
    }

    private void sendFile(
            OutputStream remoteOut, InputStream remoteIn,
            File sourceFile, String fileName)
            throws IOException, LpException {
        LOG.debug("Sending file: {} ... ", fileName);
        if (fileName.indexOf('/') > 0) {
            throw new IllegalArgumentException("File name '" + fileName + "'");
        }
        // Send command for new file.
        final Long fileSize = sourceFile.length();
        String command = "C0644 " + fileSize + " " + fileName + "\n";
        remoteOut.write(command.getBytes());
        remoteOut.flush();
        checkResponseStream(remoteIn);
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
        checkResponseStream(remoteIn);
        LOG.debug("Sending file: {} ... done", fileName);
    }

    private void checkResponseStream(InputStream stream)
            throws IOException, LpException {
        int response = stream.read();
        switch (response) {
            case -1: // No response from server.
                throw new LpException("No response from server!");
            case 0: // Success.
                break;
            case 1:
                throw new LpException("Error: {}", readResponse(stream));
            case 2:
                throw new LpException("Fatal error: {}", readResponse(stream));
            default:
                throw new LpException("Invalid response: {}", response);
        }
    }

    @Override
    public void close() {
        if (this.session != null) {
            this.session.disconnect();
        }
    }

}
