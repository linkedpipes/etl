package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Buffered output handler.
 *
 * @author Petr Škoda
 */
class BufferedOutput implements StatementConsumer {

    private final static int BUFFER_SIZE = 50000;

    private final static ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    private final WritableSingleGraphDataUnit dataUnit;

    private final IRI graph;

    private final List<Statement> buffer = new ArrayList<>(BUFFER_SIZE);

    BufferedOutput(WritableSingleGraphDataUnit dataUnit) {
        this.dataUnit = dataUnit;
        graph = dataUnit.getGraph();
    }

    @Override
    public void onRowStart() {
        // No operation here.
    }

    @Override
    public void onRowEnd() throws NonRecoverableException {
        if (buffer.size() > BUFFER_SIZE * 0.9) {
            flushBuffer();
        }
    }

    @Override
    public void onFileStart() throws NonRecoverableException {
        // No operation here.
    }

    @Override
    public void onFileEnd() throws NonRecoverableException {
        flushBuffer();
    }

    @Override
    public void submit(Resource subject, IRI predicate, Value object) {
        buffer.add(VALUE_FACTORY.createStatement(subject, predicate, object,
                graph));
    }

    private void flushBuffer() throws NonRecoverableException {
        dataUnit.execute((connection) -> {
            connection.add(buffer, graph);
        });
        buffer.clear();
    }

}
