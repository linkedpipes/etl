package com.linkedpipes.plugin.transformer.tabularuv;

import com.linkedpipes.etl.dataunit.core.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

public class RdfWriter {

    private final static int BUFFER_SIZE = 50000;

    private final static ValueFactory VALUE_FACTORY
            = SimpleValueFactory.getInstance();

    private final WritableGraphListDataUnit dataUnit;

    private IRI graph;

    private final List<Statement> buffer = new ArrayList<>(BUFFER_SIZE);

    public RdfWriter(WritableGraphListDataUnit dataUnit) {
        this.dataUnit = dataUnit;
    }

    public void setGraph(IRI graph) throws LpException {
        flushBuffer();
        this.graph = graph;
    }

    public void add(Resource subject, IRI predicate, Value object)
            throws LpException {
        buffer.add(VALUE_FACTORY.createStatement(subject, predicate, object,
                graph));
        if (buffer.size() > BUFFER_SIZE * 0.9) {
            flushBuffer();
        }
    }

    public void flush() throws LpException {
        flushBuffer();
    }

    private void flushBuffer() throws LpException {
        dataUnit.execute((connection) -> {
            connection.add(buffer, graph);
        });
        buffer.clear();
    }

}
