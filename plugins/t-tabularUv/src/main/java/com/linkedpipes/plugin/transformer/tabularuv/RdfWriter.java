package com.linkedpipes.plugin.transformer.tabularuv;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
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
 *
 * @author Petr Škoda
 */
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

    public void setGraph(IRI graph) throws NonRecoverableException {
        flushBuffer();
        this.graph = graph;
    }

    public void add(Resource subject, IRI predicate, Value object)
            throws NonRecoverableException {
        buffer.add(VALUE_FACTORY.createStatement(subject, predicate, object,
                graph));
        if (buffer.size() > BUFFER_SIZE * 0.9) {
            flushBuffer();
        }
    }

    public void flush() throws NonRecoverableException {
        flushBuffer();
    }

    private void flushBuffer() throws NonRecoverableException {
        dataUnit.execute((connection) -> {
            connection.add(buffer, graph);
        });
        buffer.clear();
    }

}
