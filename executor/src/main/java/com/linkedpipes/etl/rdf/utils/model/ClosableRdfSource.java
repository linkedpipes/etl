package com.linkedpipes.etl.rdf.utils.model;

public interface ClosableRdfSource extends BackendRdfSource {

    void close();

}
