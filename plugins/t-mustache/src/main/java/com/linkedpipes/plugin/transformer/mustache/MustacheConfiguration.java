package com.linkedpipes.plugin.transformer.mustache;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = MustacheVocabulary.CONFIG)
public class MustacheConfiguration {

    @RdfToPojo.Property(iri = MustacheVocabulary.HAS_CLASS)
    private String resourceClass;

    @RdfToPojo.Property(iri = MustacheVocabulary.HAS_TEMPLATE)
    private String template;

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

}
