package com.linkedpipes.etl.library.template.plugin.model;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public enum PluginType {
    QUALITY("http://etl.linkedpipes.com/ontology/component/type/Quality"),
    EXECUTOR("http://etl.linkedpipes.com/ontology/component/type/Extractor"),
    LOADER("http://etl.linkedpipes.com/ontology/component/type/Loader"),
    TRANSFORMER("http://etl.linkedpipes.com/ontology/component/type/Transformer"),
    OTHER("http://etl.linkedpipes.com/ontology/component/type/Executor");

    private final IRI identifier;

    PluginType(String iriAsStr) {
        this.identifier = SimpleValueFactory.getInstance().createIRI(iriAsStr);
    }

    public String asStr() {
        return identifier.stringValue();
    }

    public IRI asIri() {
        return identifier;
    }

    public static PluginType fromIri(String iriAsStr) {
        for (PluginType item : PluginType.values()) {
            if (item.identifier.stringValue().equals(iriAsStr)) {
                return item;
            }
        }
        return null;
    }

}
