package com.linkedpipes.plugin.transformer.mustachechunked;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = MustacheVocabulary.CONFIG)
public class MustacheConfiguration {

    @RdfToPojo.Property(iri = MustacheVocabulary.HAS_CLASS)
    private String resourceClass;

    @RdfToPojo.Property(iri = MustacheVocabulary.HAS_TEMPLATE)
    private String template;

    @RdfToPojo.Property(iri = MustacheVocabulary.HAS_ADD_FIRST_FLAG)
    private boolean addFirstToCollection = false;

    @RdfToPojo.Property(iri = MustacheVocabulary.HAS_ESCAPE_FOR_JSON)
    private boolean escapeForJson = false;

    public MustacheConfiguration() {
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isAddFirstToCollection() {
        return addFirstToCollection;
    }

    public void setAddFirstToCollection(boolean addFirstToCollection) {
        this.addFirstToCollection = addFirstToCollection;
    }

    public boolean isEscapeForJson() {
        return escapeForJson;
    }

    public void setEscapeForJson(boolean escapeForJson) {
        this.escapeForJson = escapeForJson;
    }

}
