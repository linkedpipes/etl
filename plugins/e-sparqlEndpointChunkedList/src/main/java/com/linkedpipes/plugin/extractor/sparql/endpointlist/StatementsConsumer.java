package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import java.util.List;
import org.eclipse.rdf4j.model.Statement;

class StatementsConsumer {

    private final WritableChunkedTriples outputRdf;

    public StatementsConsumer(WritableChunkedTriples outputRdf) {
        this.outputRdf = outputRdf;
    }

    public synchronized void consume(List<Statement> statements) throws LpException {
        outputRdf.submit(statements);
    }
}
