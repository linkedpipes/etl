package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class JarTemplateRef extends Template implements PojoLoader.Loadable {

    public static final IRI TYPE;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        TYPE = valueFactory.createIRI(LP_PIPELINE.JAS_TEMPLATE);
    }

    private boolean supportControl;

    private String configurationDescription;

    @Override
    public void loadIri(String iri) {
        this.iri = iri;
    }

    @Override
    public PojoLoader.Loadable load(String predicate, Value value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_SUPPORT_CONTROL:
                supportControl = ((Literal) value).booleanValue();
                break;
            case LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION:
                configurationDescription = value.stringValue();
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Type getType() {
        return Type.JAR_TEMPLATE;
    }

    @Override
    public boolean isSupportingControl() {
        return supportControl;
    }

    @Override
    public String getConfigurationDescription() {
        return configurationDescription;
    }

}
