package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.Statement;

import java.util.List;

class StatementsConsumer {

    private final WritableSingleGraphDataUnit outputRdf;

    public StatementsConsumer(WritableSingleGraphDataUnit outputRdf) {
        this.outputRdf = outputRdf;
    }

    public synchronized void consume(List<Statement> statements)
            throws LpException {
        outputRdf.execute((connection) -> {
            connection.begin();
            connection.add(statements, outputRdf.getWriteGraph());
            connection.commit();
        });
    }

}