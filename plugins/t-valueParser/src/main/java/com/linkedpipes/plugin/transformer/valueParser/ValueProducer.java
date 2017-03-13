package com.linkedpipes.plugin.transformer.valueParser;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

abstract class ValueProducer {

    protected final WritableSingleGraphDataUnit output;

    protected final IRI graph;

    protected Resource resource;

    protected final IRI predicate;

    protected ValueFactory valueFactory = SimpleValueFactory.getInstance();

    protected List<Statement> buffer = new ArrayList<>(16);

    public ValueProducer(WritableSingleGraphDataUnit output, String predicate) {
        this.output = output;
        this.graph = output.getWriteGraph();
        this.predicate = valueFactory.createIRI(predicate);
    }

    public abstract void onValue(String value);

    public void onEntityStart(Resource resource, Value value) {
        this.resource = resource;
    }

    public void onEntityEnd() throws LpException {
        output.execute((connection) -> {
            connection.add(buffer, graph);
            buffer.clear();
        });
    }

}
