package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Represent a thin template that can modify basic component
 * properties and configuration.
 */
class ReferenceTemplate extends Template
        implements PojoLoader.Loadable {

    public static final IRI TYPE;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        TYPE = valueFactory.createIRI(LP_PIPELINE.REFERENCE_TEMPLATE);
    }

    /**
     * Template for this template.
     */
    private String template;

    private String configurationDescription;

    public String getTemplate() {
        return template;
    }

    @Override
    public void loadIri(String iri) {
        this.iri = iri;
    }

    @Override
    public PojoLoader.Loadable load(String predicate, Value value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_TEMPLATE:
                template = value.stringValue();
                break;
            case LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION:
                configurationDescription = value.stringValue();
                break;
        }
        return null;
    }

    @Override
    public String getIri() {
        return iri;
    }

    @Override
    public Type getType() {
        return Type.REFERENCE_TEMPLATE;
    }

    @Override
    public boolean isSupportingControl() {
        // Every reference support control as its parent support control,
        // because we have template from it.
        return true;
    }

    @Override
    public String getConfigurationDescription() {
        return configurationDescription;
    }

}
