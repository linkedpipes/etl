package com.linkedpipes.plugin.loader.graphstoreprotocol;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

public class Entry {

    @RdfToPojo.Property(iri = GraphStoreProtocolVocabulary.HAS_FILE_NAME)
    public String fileName;

    @RdfToPojo.Property(iri = GraphStoreProtocolVocabulary.HAS_GRAPH)
    public String targetGraph;

}
