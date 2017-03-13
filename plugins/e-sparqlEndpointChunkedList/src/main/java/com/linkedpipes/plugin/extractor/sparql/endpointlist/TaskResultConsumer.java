package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.Statement;

import java.util.List;

class TaskResultConsumer {

    private final WritableChunkedTriples outputRdf;

    public TaskResultConsumer(WritableChunkedTriples outputRdf) {
        this.outputRdf = outputRdf;
    }

    public synchronized void consume(List<Statement> statements)
            throws LpException {
        outputRdf.submit(statements);
    }

}
