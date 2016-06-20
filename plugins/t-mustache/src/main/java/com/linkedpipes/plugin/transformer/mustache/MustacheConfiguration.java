package com.linkedpipes.plugin.transformer.mustache;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = MustacheVocabulary.CONFIG)
public class MustacheConfiguration {

    @RdfToPojo.Property(uri = MustacheVocabulary.HAS_CLASS)
    private String resourceClass;

    @RdfToPojo.Property(uri = MustacheVocabulary.HAS_TEMPLATE)
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
