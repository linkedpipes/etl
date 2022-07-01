package com.linkedpipes.etl.executor.api.v1.rdf.model;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;

import java.util.Date;

public interface TripleWriter {

    void iri(String subject, String predicate, String object);

    void string(String subject, String predicate, String object);

    void string(String subject, String predicate, String object, String lang);

    void date(String subject, String predicate, Date object);

    void typed(String subject, String predicate, String object, String type);

    void flush() throws RdfException;

}
