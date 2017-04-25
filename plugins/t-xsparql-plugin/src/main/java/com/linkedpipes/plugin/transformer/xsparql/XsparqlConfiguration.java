package com.linkedpipes.plugin.transformer.xsparql;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by patzo on 11.04.17.
 */
@RdfToPojo.Type(iri = XsparqlVocabulary.CONFIG_CLASS)
public class XsparqlConfiguration {
    @RdfToPojo.Property(iri = XsparqlVocabulary.TEMPLATE)
    private String xsparqlQuery = "";

    @RdfToPojo.Property(iri = XsparqlVocabulary.EXTENSION)
    private String newExtension;

    @RdfToPojo.Type(iri = XsparqlVocabulary.REFERENCE)
    public static class Reference {

        @RdfToPojo.Property(iri = XsparqlVocabulary.HAS_KEY)
        private String key;

        @RdfToPojo.Property(iri = XsparqlVocabulary.HAS_VAL)
        private String val;

        public Reference() {
        }
        public void setKey(String key){ this.key = key; }
        public void setVal(String val){ this.val = val; }
        public String getKey(){return key;};
        public String getVal(){return val;}
    }

    public XsparqlConfiguration() {
        newExtension = "ttl";
    }

    @RdfToPojo.Property(iri = XsparqlVocabulary.HAS_REFERENCE)
    private List<Reference> references = new LinkedList<>();

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(
            List<Reference> references) {
        this.references = references;
    }

    public String getXsparqlQuery() {
        return xsparqlQuery;
    }

    public void setXsparqlQuery(String xsparqlQuery) {
        this.xsparqlQuery = xsparqlQuery;
    }

    public String getNewExtension() {
        return newExtension;
    }

    public void setNewExtension(String newExtension) {
        this.newExtension = newExtension;
    }

}
