package com.linkedpipes.plugin.loader.wikibase.model;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ReferenceBuilder;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.rdf.PropertyRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RdfToReference {

    private ReferenceBuilder builder;

    private List<String> types;

    private Collection<Statement> statements;

    private final PropertyRegister register;

    private final String entityPrefix;

    private final String propPrefix;

    private final Map<Object, MergeStrategy> mergeStrategy;

    public RdfToReference(
            PropertyRegister register,String entityPrefix, String propPrefix,
            Map<Object, MergeStrategy> mergeStrategy) {
        this.register = register;
        this.entityPrefix = entityPrefix;
        this.propPrefix = propPrefix;
        this.mergeStrategy = mergeStrategy;
    }

    public Reference loadReference(
            Resource resource, Collection<Statement> statements) {
        builder = ReferenceBuilder.newInstance();
        types = new ArrayList<>();
        this.statements = statements;

        String shortValuePrefix = propPrefix + "reference/";
        String valuePrefix = propPrefix + "reference/value/";

        statements.stream()
                .filter((st) -> st.getSubject().equals(resource))
                .forEach((st) -> {
                    String predicate = st.getPredicate().stringValue();
                    if (st.getPredicate().equals(RDF.TYPE)) {
                        types.add(st.getObject().stringValue());
                        return;
                    }
                    PropertyIdValue property =
                            iriToPropertyId(st.getPredicate());
                    if (predicate.startsWith(valuePrefix)) {
                        loadReference(property, st.getObject());
                    } else if (predicate.startsWith(shortValuePrefix)) {
                        loadValueShort(property, st.getObject());
                    }
                });

        checkForNoValue();
        Reference result = builder.build();
        selectMergerStrategy(result);
        return result;
    }

    private PropertyIdValue iriToPropertyId(IRI iri) {
        String strValue = iri.stringValue();
        String id = strValue.substring(strValue.lastIndexOf("/") + 1);
        return Datamodel.makePropertyIdValue(id, propPrefix);
    }

    private void loadValueShort(
            PropertyIdValue property, Value rdfValue) {
        String type = register.getPropertyType(property);
        org.wikidata.wdtk.datamodel.interfaces.Value value =
                RdfToValue.get(type).getValue(rdfValue, type);
        builder.withPropertyValue(storeProperty(property), value);
    }

    private void loadReference(
            PropertyIdValue property, Value rdfValue) {
        String type = register.getPropertyType(property);
        if (rdfValue instanceof BNode) {
            builder.withSomeValue(property);
            return;
        } else if (!(rdfValue instanceof Resource)) {
            // TODO Invalid schema.
            return;
        }
        Resource rdfResource = (Resource) rdfValue;
        org.wikidata.wdtk.datamodel.interfaces.Value value =
                RdfToValue.get(type).getValue(statements, rdfResource);
        builder.withPropertyValue(storeProperty(property), value);
    }

    private PropertyIdValue storeProperty(PropertyIdValue property) {
        // We use entity to be compatible with wdtk library.
        return Datamodel.makePropertyIdValue(property.getId(), entityPrefix);
    }

    private void checkForNoValue() {
        String prefix = propPrefix + "novalue/";
        for (String type : types) {
            if (!type.startsWith(prefix)) {
                continue;
            }
            String id = type.substring(prefix.length());
            PropertyIdValue property =
                    Datamodel.makePropertyIdValue(id, propPrefix);
            builder.withNoValue(storeProperty(property));
        }
    }

    private void selectMergerStrategy(Reference reference) {
        mergeStrategy.put(reference, MergeStrategy.fromTypes(types));
    }

}
