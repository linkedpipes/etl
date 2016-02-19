package com.linkedpipes.executor.monitor.execution.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.commons.entities.executor.DebugStructure;
import com.linkedpipes.executor.monitor.execution.entity.ExecutionMetadata;
import java.io.File;
import java.io.IOException;

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
        throw new UnsupportedOperationException();
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
