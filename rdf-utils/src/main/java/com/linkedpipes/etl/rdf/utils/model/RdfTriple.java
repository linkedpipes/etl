package com.linkedpipes.etl.rdf.utils.model;

/**
 * Library independent representation of a RDF triple.
 */
public interface RdfTriple {

    String getSubject();

    String getPredicate();

    RdfValue getObject();

}
