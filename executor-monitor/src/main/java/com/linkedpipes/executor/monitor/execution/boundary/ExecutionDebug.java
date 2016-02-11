package com.linkedpipes.executor.monitor.execution.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.linkedpipes.commons.entities.executor.DebugStructure;
import com.linkedpipes.executor.monitor.execution.entity.ExecutionMetadata;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains debug information about an execution.
 *
 * @author Petr Škoda
 */
public class ExecutionDebug {

    public static class InvalidResource extends Exception {

        public InvalidResource(String message) {
            super(message);
        }

        public InvalidResource(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionDebug.class);

    private final ExecutionMetadata execution;

    private DebugStructure debugStructure = null;

    ExecutionDebug(ExecutionMetadata execution) {
        this.execution = execution;
    }

    /**
     *
     * @param dataUnitId
     * @return
     * @throws com.linkedpipes.executor.monitor.execution.boundary.ExecutionDebug.InvalidResource
     */
    public File getRdfDump(String dataUnitId) throws InvalidResource {
        if (debugStructure == null) {
            loadDebugStructure();
        }
        //
        final DebugStructure.DataUnit dataUnit = debugStructure.getDataUnits().get(dataUnitId);
        if (dataUnit == null) {
            LOG.warn("Missing data unit: {} for execution: {}", dataUnitId, execution.getId());
            throw new InvalidResource("Missing data unit.");
        }

        // TODO Check file type!

        // Load info file.
        String rdfDataFilePath = null;
        final File infoFile = new File(new File(URI.create(dataUnit.getDebugDirectory())), "info.dat");
        try {
            final List<String> lines = Files.readLines(infoFile, Charset.forName("UTF-8"));
            if (lines.size() != 1) {
                throw new InvalidResource("inalid number of lines: " + Integer.toString(lines.size()));
            } else {
                rdfDataFilePath = lines.get(0);
            }
        } catch (IOException ex) {
            throw new InvalidResource("Can't read info.dat file.", ex);
        }
        //
        if (rdfDataFilePath == null) {
            throw new InvalidResource("Variable rdfDataFilePath is null.");
        }
        final File rdfDataFile =  new File(rdfDataFilePath, "data.trig");
        if (rdfDataFile.exists()) {
            return rdfDataFile;
        } else {
            throw new InvalidResource("Missing data.trig file.");
        }
    }

    /**
     * Load debug structure.
     *
     * @throws com.linkedpipes.executor.monitor.execution.boundary.ExecutionDebug.InvalidResource
     */
    protected void loadDebugStructure() throws InvalidResource {
        // Load debug file.
        final File debugFile = new File(execution.getDirectory(), "dump/debug.json");
        final ObjectMapper json = new ObjectMapper();
        try {
            this.debugStructure = json.readValue(debugFile, DebugStructure.class);
        } catch (IOException ex) {
            throw new InvalidResource("Can't read debug.json file.", ex);
        }
    }

}
