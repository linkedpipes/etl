package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.Statement;

import java.util.Iterator;
import java.util.List;

class ExecutorManager {

    private final Iterator<ChunkedTriples.Chunk> inputChunks;

    private final WritableChunkedTriples outputRdf;

    private final ProgressReport progressReport;

    private boolean terminate = false;

    public ExecutorManager(ChunkedTriples inputRdf,
            WritableChunkedTriples outputRdf,
            ProgressReport progressReport) {
        this.inputChunks = inputRdf.iterator();
        this.outputRdf = outputRdf;
        this.progressReport = progressReport;
    }

    public void terminate() {
        terminate = true;
    }

    /**
     * @return Null if there is no aditional task to execute.
     */
    public synchronized ChunkedTriples.Chunk getChunk() throws LpException {
        if (terminate) {
            return null;
        }
        if (inputChunks.hasNext()) {
            return inputChunks.next();
        } else {
            return null;
        }
    }

    public synchronized void submitResult(List<Statement> statements)
            throws LpException {
        outputRdf.submit(statements);
        progressReport.entryProcessed();
    }

}
