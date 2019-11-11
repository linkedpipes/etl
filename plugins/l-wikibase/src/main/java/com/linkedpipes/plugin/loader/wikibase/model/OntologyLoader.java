package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;

import java.util.HashMap;
import java.util.Map;

public class OntologyLoader {

    public Map<String, Property> loadProperties(RdfSource source)
            throws RdfException {
        Map<String, Property> result = new HashMap<>();
        for (String iri : source.getByType(Wikidata.PROPERTY)) {
            String predicate = iri.substring(iri.lastIndexOf("/") + 1);
            result.put(predicate, loadProperty(source, iri));
        }
        return result;
    }

    protected Property loadProperty(RdfSource source, String iri)
            throws RdfException {
        Property property = new Property(iri);
        source.statements(iri, (predicate, value) -> {
            if (Wikidata.PROPERTY_TYPE.equals(predicate)) {
                property.setType(value.asString());
            }
        });
        return property;
    }

}
