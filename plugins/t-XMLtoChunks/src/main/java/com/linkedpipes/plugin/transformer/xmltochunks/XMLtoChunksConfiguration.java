package com.linkedpipes.plugin.transformer.xmltochunks;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by patzo on 18.04.17.
 */
@RdfToPojo.Type(iri = XMLtoChunksVocabulary.CONFIG_CLASS)
public class XMLtoChunksConfiguration {
    @RdfToPojo.Property(iri = XMLtoChunksVocabulary.CHUNK_SIZE)
    private int chunk_size = 150;


    public XMLtoChunksConfiguration() {
    }

    public int getChunk_size() {
        return chunk_size;
    }

    public void setChunk_size(int chunkSize) {
        this.chunk_size = chunkSize;
    }

    @RdfToPojo.Type(iri = XMLtoChunksVocabulary.REFERENCE)
    public static class Reference {
        @RdfToPojo.Property(iri = XMLtoChunksVocabulary.HAS_PREFIX)
        private String prefix;

        @RdfToPojo.Property(iri = XMLtoChunksVocabulary.HAS_LOCAL)
        private String local;

        public Reference() {}

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
        public String getPrefix() {
            return prefix;
        }
        public void setLocal(String local) { this.local = local; }
        public String getLocal() {
            return local;
        }
    }

    @RdfToPojo.Property(iri = XMLtoChunksVocabulary.HAS_REFERENCE)
    private List<Reference> references = new LinkedList<>();

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(
            List<Reference> references) {
        this.references = references;
    }
}
