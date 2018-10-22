package com.linkedpipes.plugin.transformer.property.linker;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = PropertyLinkerVocabulary.CONFIG)
public class PropertyLinkedConfiguration {

    @RdfToPojo.Property(iri = PropertyLinkerVocabulary.HAS_CHUNK_PREDICATE)
    private String chunkPredicate;

    @RdfToPojo.Property(iri = PropertyLinkerVocabulary.HAS_DATA_PREDICATE)
    private String dataPredicate;

    public PropertyLinkedConfiguration() {
    }

    public String getChunkPredicate() {
        return chunkPredicate;
    }

    public void setChunkPredicate(String chunkPredicate) {
        this.chunkPredicate = chunkPredicate;
    }

    public String getDataPredicate() {
        return dataPredicate;
    }

    public void setDataPredicate(String dataPredicate) {
        this.dataPredicate = dataPredicate;
    }

}
