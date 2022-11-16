package com.linkedpipes.etl.rdf.utils.model;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

public interface BackendTripleWriter {

    void iri(String subject, String predicate, String object);

    void bool(String subject, String predicate, boolean object);

    void string(
            String subject, String predicate, String object, String language);

    void typed(String subject, String predicate, String object, String type);

    void add(String subject, String predicate, BackendRdfValue value);

    void add(RdfTriple triple);

    /**
     * Add triples from inner buffer to source. Clear inner buffer.
     */
    void flush() throws RdfUtilsException;
}
