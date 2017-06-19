package com.linkedpipes.etl.rdf.utils.model;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

import java.util.List;

public class SimpleStore implements RdfSource {

    private List<RdfTriple> triples;

    public SimpleStore(List<RdfTriple> triples) {
        this.triples = triples;
    }

    @Override
    public TripleWriter getTripleWriter(String graph) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void triples(String graph, TripleHandler handler)
            throws RdfUtilsException {
        for (RdfTriple triple : triples) {
            try {
                handler.handle(triple);
            } catch (Exception ex) {
                throw new RdfUtilsException("", ex);
            }
        }
    }

    @Override
    public void triples(String resource, String graph, TripleHandler handler)
            throws RdfUtilsException {
        for (RdfTriple triple : triples) {
            if (!triple.getSubject().equals(resource)) {
                continue;
            }
            try {
                handler.handle(triple);
            } catch (Exception ex) {
                throw new RdfUtilsException("", ex);
            }
        }
    }

    @Override
    public SparqlQueryable asQueryable() {
        throw new UnsupportedOperationException();
    }
}
