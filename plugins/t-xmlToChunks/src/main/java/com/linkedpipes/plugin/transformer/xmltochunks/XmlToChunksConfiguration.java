package com.linkedpipes.plugin.transformer.xmltochunks;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = XmlToChunksVocabulary.CONFIG_CLASS)
public class XmlToChunksConfiguration {

    @RdfToPojo.Type(iri = XmlToChunksVocabulary.REFERENCE)
    public static class Reference {

        @RdfToPojo.Property(iri = XmlToChunksVocabulary.HAS_PREFIX)
        private String prefix;

        @RdfToPojo.Property(iri = XmlToChunksVocabulary.HAS_LOCAL)
        private String local;

        public Reference() {
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setLocal(String local) {
            this.local = local;
        }

        public String getLocal() {
            return local;
        }
    }

    @RdfToPojo.Property(iri = XmlToChunksVocabulary.CHUNK_SIZE)
    private int chunk_size = 150;

    @RdfToPojo.Property(iri = XmlToChunksVocabulary.HAS_REFERENCE)
    private List<Reference> references = new LinkedList<>();

    public XmlToChunksConfiguration() {
    }

    public int getChunk_size() {
        return chunk_size;
    }

    public void setChunk_size(int chunkSize) {
        this.chunk_size = chunkSize;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }
}
