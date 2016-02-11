package com.linkedpipes.executor.monitor.process.entity;

import com.linkedpipes.commons.entities.executor.monitor.ExternalProcess;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class FusekiProcess extends BaseProcess {

    private static final Logger LOG = LoggerFactory.getLogger(FusekiProcess.class);

    /**
     * Directory to delete if process is terminated.
     */
    private final File directory;

    public FusekiProcess(File directory, ExternalProcess externalProcess, Process systemProcess, Integer port) {
        super(externalProcess, systemProcess, port);
        this.directory = directory;
    }

    @Override
    public void terminate() {
        systemProcess.destroyForcibly();
        try {
            // Wait for termination so we can delete this instance.
            systemProcess.waitFor();
        } catch (InterruptedException ex) {
            // Ignore.
        }
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException ex) {
            LOG.error("Can't delete Fuseki working directory.", ex);
        }
    }

}
