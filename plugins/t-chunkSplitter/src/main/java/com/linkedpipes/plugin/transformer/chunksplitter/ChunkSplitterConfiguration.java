package com.linkedpipes.plugin.transformer.chunksplitter;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = ChunkSplitterVocabulary.CONFIG)
public class ChunkSplitterConfiguration {

    @RdfToPojo.Property(iri = ChunkSplitterVocabulary.HAS_TYPE)
    private String type;

    public ChunkSplitterConfiguration() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
