package com.linkedpipes.etl.rdf.utils.model;

@FunctionalInterface
public interface TripleHandler {

    void handle(RdfTriple triple) throws Exception;

}
