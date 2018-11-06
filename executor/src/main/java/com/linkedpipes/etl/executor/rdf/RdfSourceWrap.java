package com.linkedpipes.etl.executor.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

public class RdfSourceWrap implements RdfSource {

    private final BackendRdfSource source;

    private final String graph;

    public RdfSourceWrap(BackendRdfSource source, String graph) {
        this.source = source;
        this.graph = graph;
    }

    @Override
    public void statements(String subject, StatementHandler handler)
            throws RdfException {
        try {
            source.triples(subject, graph, (triple) -> {
                RdfValueWrap value = new RdfValueWrap(triple.getObject());
                handler.accept(triple.getPredicate(), value);
            });
        } catch (RdfUtilsException ex) {
            throw new RdfException("", ex);
        }
    }

    @Override
    public List<RdfValue> getPropertyValues(String subject, String predicate)
            throws RdfException {
        List<RdfValue> result = new ArrayList<>();
        try {
            source.triples(subject, graph, (triple) -> {
                if (triple.getPredicate().equals(predicate)) {
                    result.add(new RdfValueWrap(triple.getObject()));
                }
            });
        } catch (RdfUtilsException ex) {
            throw new RdfException("", ex);
        }
        return result;
    }

    @Override
    public List<String> getByType(String type) throws RdfException {
        List<String> result = new ArrayList<>();
        try {
            source.triples(null, graph, (triple) -> {
                if (triple.getPredicate().equals(RDF.TYPE)
                        && triple.getObject().asString().equals(type)) {
                    result.add(triple.getSubject());
                }
            });
        } catch (RdfUtilsException ex) {
            throw new RdfException("", ex);
        }
        return result;
    }

}
