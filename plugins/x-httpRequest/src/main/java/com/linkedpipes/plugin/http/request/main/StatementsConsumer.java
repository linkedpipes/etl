package com.linkedpipes.plugin.http.request.main;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.Statement;

import java.util.List;

public class StatementsConsumer {

    private final WritableSingleGraphDataUnit outputRdf;

    private final Object lock = new Object();

    public StatementsConsumer(WritableSingleGraphDataUnit outputRdf) {
        this.outputRdf = outputRdf;
    }

    public void write(List<Statement> statements) throws LpException {
        synchronized (lock) {
            outputRdf.execute((connection) -> {
                connection.begin();
                connection.add(statements, outputRdf.getWriteGraph());
                connection.commit();
            });
        }
    }

}
