package com.linkedpipes.plugin.loader.scp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.etl.executor.api.v1.exception.RecoverableException;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.AfterExecution;
import com.linkedpipes.etl.dpu.api.extensions.FaultTolerance;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Used library: http://www.jcraft.com/jsch/examples/ SCP protocol:
 * https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works
 *
 * JSCH also supports SFTP, example on the project site and on
 * http://stackoverflow.com/questions/2346764/java-sftp-transfer-library
 *
 * @author Petr Škoda
 */
public final class LoaderScp implements SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(LoaderScp.class);

    @DataProcessingUnit.OutputPort(id = "FilesInput")
    public FilesDataUnit input;

    @DataProcessingUnit.Configuration
    public LoaderScpConfiguration configuration;

    @DataProcessingUnit.Extension
    public FaultTolerance faultTolerance;

    @DataProcessingUnit.Extension
    public AfterExecution cleanUp;

    @Override
    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException {
        final String user = configuration.getUserName();
        final String password = configuration.getPassword();
        final String host = configuration.getHost();
        final int port = configuration.getPort();
        final String targetFile = configuration.getTargetDirectory();

        final JSch jsch = new JSch();

        // Create session.
        final Session session = faultTolerance.call(() -> jsch.getSession(user, host, port));
        session.setPassword(password);

        // Enable connection to machines with unknown host key - this is potential secutiry risk!
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        faultTolerance.call(() -> session.connect());
        cleanUp.addAction(() -> session.disconnect());

        if (configuration.isCreateDirectory()) {
            faultTolerance.call(() -> secureCreateDirectory(session, targetFile));
        }

        // Execute 'scp -t targetFile' on remote machine and get related streams.
        final Channel channel = faultTolerance.call(() -> session.openChannel("exec"));

        // File transfer.
        // -r - enable copy of empty directory
        //  echo D0755 0 testdir;
        //  echo E
        // -d - means directory transfer
        ((ChannelExec) channel).setCommand("scp -r -t -d " + targetFile);
        try (OutputStream remoteOut = channel.getOutputStream(); InputStream remoteIn = channel.getInputStream()) {
            channel.connect();
            resonseCheck(remoteIn);
            // Send content of files data unit.
            for (File rootDirectory : input.getReadRootDirectories()) {
                sendDirectoryContent(remoteOut, remoteIn, rootDirectory);
            }
        } catch (IOException | JSchException | RecoverableException ex) {
            throw new DataProcessingUnit.ExecutionFailed("Can't upload data!", ex);
        } finally {
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    /**
     * Experimental function create target directory.
     *
     * @param session
     * @param targetPath
     * @throws JSchException
     * @throws IOException
     */
    private static void secureCreateDirectory(Session session, String targetPath) throws JSchException, IOException {
        LOG.debug("secureCreateDirectory ...");
        final Channel channel = session.openChannel("exec");
        // We just execute given command.
        ((ChannelExec) channel).setCommand("mkdir " + targetPath);
        final InputStream remoteIn = channel.getInputStream();
        channel.connect();
        LOG.debug("\tWaiting for response!");
        final StringBuffer buffer = new StringBuffer();
        int value;
        while (!channel.isConnected()) {
            value = remoteIn.read();
            buffer.append((char) value);
            if (value != '\n' || value != 0) {
                break;
            }
        }
        LOG.debug("\tResponse: {}", buffer.toString());
        channel.disconnect();
        LOG.debug("secureCreateDirectory ... done");
    }

    /**
     * Send content of given directory.
     *
     * @param remoteOut
     * @param remoteIn
     * @param sourceDirectory
     * @throws IOException
     * @throws com.linkedpipes.etl.dpu.api.DataProcessingUnit.ExecutionFailed
     * @throws RecoverableException
     */
    private static void sendDirectoryContent(OutputStream remoteOut, InputStream remoteIn, File sourceDirectory)
            throws IOException, DataProcessingUnit.ExecutionFailed, RecoverableException {
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
     * @throws IOException
     * @throws com.linkedpipes.etl.dpu.api.DataProcessingUnit.ExecutionFailed
     * @throws RecoverableException
     */
    private static void sendDirectory(OutputStream remoteOut, InputStream remoteIn, File sourceDirectory,
            String directoryName) throws IOException, DataProcessingUnit.ExecutionFailed, RecoverableException {
        LOG.debug("Sending directory: {} ... ", directoryName);
        // Send command.
        String command = "D0755 0 " + directoryName + "\n";
        remoteOut.write(command.getBytes());
        remoteOut.flush();
        resonseCheck(remoteIn);
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
        resonseCheck(remoteIn);
        LOG.debug("Sending directory: {} ... done", directoryName);
    }

    /**
     *
     * @param remoteOut
     * @param remoteIn
     * @param sourceFile
     * @param fileName Must not include '/'.
     * @throws java.io.IOException
     * @throws com.linkedpipes.etl.dpu.api.DataProcessingUnit.ExecutionFailed
     * @throws com.linkedpipes.etl.executor.api.v1.exception.RecoverableException
     */
    private static void sendFile(OutputStream remoteOut, InputStream remoteIn, File sourceFile, String fileName)
            throws IOException, DataProcessingUnit.ExecutionFailed, RecoverableException {
        LOG.debug("Sending file: {} ... ", fileName);
        if (fileName.indexOf('/') > 0) {
            throw new IllegalArgumentException("File name '" + fileName + "'");
        }
        // Send command for new file.
        final Long fileSize = sourceFile.length();
        String command = "C0644 " + fileSize + " " + fileName + "\n";
        remoteOut.write(command.getBytes());
        remoteOut.flush();
        resonseCheck(remoteIn);
        // Copy file.
        try (FileInputStream sourceFileStream = new FileInputStream(sourceFile)) {
            IOUtils.copy(sourceFileStream, remoteOut);
        }
        remoteOut.flush();
        // Write '\0' as the end of file.
        remoteOut.write(0);
        remoteOut.flush();
        // Check satus.
        resonseCheck(remoteIn);
        LOG.debug("Sending file: {} ... done", fileName);
    }

    /**
     * Check response from SCP server. Throws exception in case of error.
     *
     * @param stream
     * @throws IOException
     * @throws com.linkedpipes.etl.dpu.api.DataProcessingUnit.ExecutionFailed
     * @throws com.linkedpipes.etl.executor.api.v1.exception.RecoverableException
     */
    private static void resonseCheck(InputStream stream) throws IOException,
            DataProcessingUnit.ExecutionFailed, RecoverableException {
        final int response = stream.read();
        switch (response) {
            case -1: // No response from server.
                throw new DataProcessingUnit.ExecutionFailed("No respnse from server!");
            case 0: // Success.
                break;
            case 1:
                // TODO This is non-fatal error we can recover!
                throw new DataProcessingUnit.ExecutionFailed("Error: {}", readResponseLine(stream));
            case 2:
                throw new DataProcessingUnit.ExecutionFailed("Fatal error: {}", readResponseLine(stream));
            default:
                throw new DataProcessingUnit.ExecutionFailed("Invalid reponse: {}", response);
        }
    }

    /**
     * Read line ended with new line symbol from given stream.
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private static String readResponseLine(InputStream stream) throws IOException {
        final StringBuffer buffer = new StringBuffer();
        int value;
        do {
            value = stream.read();
            buffer.append((char) value);
        } while (value != '\n' && value != 0);
        return buffer.toString();
    }

}
