package com.linkedpipes.plugin.transformer.rdftowrappedjsonldchunked;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = RdfToWrappedJsonLdChunkedVocabulary.CONFIGURATION)
public class RdfToWrappedJsonLdChunkedConfiguration {

    @RdfToPojo.Property(iri = RdfToWrappedJsonLdChunkedVocabulary.HAS_TEMPLATE)
    private String template;

    @RdfToPojo.Property(
            iri = RdfToWrappedJsonLdChunkedVocabulary.HAS_ID_RESOURCE_TYPE)
    private String mainResourceType;

    public RdfToWrappedJsonLdChunkedConfiguration() {
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getMainResourceType() {
        return mainResourceType;
    }

    public void setMainResourceType(String mainResourceType) {
        this.mainResourceType = mainResourceType;
    }

}
