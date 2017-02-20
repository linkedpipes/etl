package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Buffered output handler.
 *
 */
class BufferedOutput implements StatementConsumer {

    private final static int BUFFER_SIZE = 50000;

    private final static ValueFactory VALUE_FACTORY
            = SimpleValueFactory.getInstance();

    private final WritableSingleGraphDataUnit dataUnit;

    private final IRI graph;

    private final List<Statement> buffer = new ArrayList<>(BUFFER_SIZE);

    BufferedOutput(WritableSingleGraphDataUnit dataUnit) {
        this.dataUnit = dataUnit;
        graph = dataUnit.getWriteGraph();
    }

    @Override
    public void onRowStart() {
        // No operation here.
    }

    @Override
    public void onRowEnd() throws LpException {
        if (buffer.size() > BUFFER_SIZE * 0.9) {
            flushBuffer();
        }
    }

    @Override
    public void onFileStart() throws LpException {
        // No operation here.
    }

    @Override
    public void onFileEnd() throws LpException {
        flushBuffer();
    }

    @Override
    public void submit(Resource subject, IRI predicate, Value object) {
        buffer.add(VALUE_FACTORY.createStatement(
                subject, predicate, object, graph));
    }

    private void flushBuffer() throws LpException {
        dataUnit.execute((connection) -> {
            connection.add(buffer, graph);
        });
        buffer.clear();
    }

}
