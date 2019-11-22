package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.plugin.loader.wikibase.WikibaseLoaderVocabulary;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.rdf.PropertyRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RdfToStatement {

    private static final Logger LOG =
            LoggerFactory.getLogger(RdfToStatement.class);

    private static final String WIKIBASE_STATEMENT =
            "http://wikiba.se/ontology#Statement";

    private static String WAS_DERIVED_FROM =
            "http://www.w3.org/ns/prov#wasDerivedFrom";

    private StatementBuilder builder;

    private List<String> types;

    private Collection<Statement> statements;

    private final PropertyRegister register;

    private final String entityPrefix;

    private final String propPrefix;

    private final Map<Object, MergeStrategy> mergeStrategy;

    public RdfToStatement(
            PropertyRegister register, String entityPrefix, String propPrefix,
            Map<Object, MergeStrategy> mergeStrategy) {
        this.register = register;
        this.entityPrefix = entityPrefix;
        this.propPrefix = propPrefix;
        this.mergeStrategy = mergeStrategy;
    }

    public org.wikidata.wdtk.datamodel.interfaces.Statement loadStatement(
            ItemIdValue owner, PropertyIdValue property,
            Resource resource, Collection<Statement> statements) {
        builder =
                StatementBuilder.forSubjectAndProperty(
                        owner, storeProperty(property));
        types = new ArrayList<>();
        this.statements = statements;

        String propPrefix = property.getSiteIri();
        String shortValuePredicate =
                propPrefix + "statement/" + property.getId();
        String valuePredicate =
                propPrefix + "statement/value/" + property.getId();
        String qualifierPrefix = propPrefix + "qualifier/value/";

        statements.stream()
                .filter((st) -> st.getSubject().equals(resource))
                .forEach((st) -> {
                    String predicate = st.getPredicate().stringValue();
                    if (st.getPredicate().equals(RDF.TYPE)) {
                        types.add(st.getObject().stringValue());
                    } else if (predicate.equals(shortValuePredicate)) {
                        loadValueShort(property, st.getObject());
                    } else if (predicate.equals(valuePredicate)) {
                        loadValue(property, st.getObject());
                    } else if (predicate.startsWith(qualifierPrefix)) {
                        loadQualifier(
                                iriToPropertyId(st.getPredicate()),
                                st.getObject());
                    } else if (predicate.equals(WAS_DERIVED_FROM)) {
                        loadWasDerivedFrom(propPrefix, st.getObject());
                    }
                });

        // Set ID only if it is not a new statement.
        if (!types.contains(WikibaseLoaderVocabulary.NEW_STRATEGY)) {
            setStatementId(resource);
        }

        if (types.contains(WIKIBASE_STATEMENT) || resource instanceof BNode) {
            org.wikidata.wdtk.datamodel.interfaces.Statement result =
                    builder.build();
            selectMergerStrategy(result);
            return result;
        } else {
            return null;
        }
    }

    private PropertyIdValue storeProperty(PropertyIdValue property) {
        // We use entity to be compatible with wdtk library.
        return Datamodel.makePropertyIdValue(property.getId(), entityPrefix);
    }

    private void setStatementId(Resource resource) {
        if (resource instanceof BNode) {
            // We do not set ID here as there is none.
            return;
        }
        String strValue = resource.stringValue();
        String id = strValue.substring(strValue.lastIndexOf("/") + 1)
                .replaceFirst("-", "\\$");
        builder.withId(id);
    }

    private void loadValueShort(
            PropertyIdValue property, Value rdfValue) {
        String type = register.getPropertyType(property);
        org.wikidata.wdtk.datamodel.interfaces.Value value =
                RdfToValue.get(type).getValue(rdfValue, type);
        builder.withValue(value);
    }

    private void loadValue(
            PropertyIdValue property, Value rdfValue) {
        String type = register.getPropertyType(property);
        if (rdfValue instanceof BNode) {
            builder.withSomeValue();
            return;
        } else if (!(rdfValue instanceof Resource)) {
            LOG.warn(
                    "Ignoring simple values, as complex was expected: {}",
                    rdfValue);
            // TODO Invalid schema.
            return;
        }
        Resource rdfResource = (Resource) rdfValue;
        org.wikidata.wdtk.datamodel.interfaces.Value value =
                RdfToValue.get(type).getValue(statements, rdfResource, type);
        builder.withValue(value);
    }

    private PropertyIdValue iriToPropertyId(IRI iri) {
        String strValue = iri.stringValue();
        String id = strValue.substring(strValue.lastIndexOf("/") + 1);
        return Datamodel.makePropertyIdValue(id, propPrefix);
    }

    private void loadQualifier(
            PropertyIdValue property, Value rdfValue) {
        String type = register.getPropertyType(property);
        if (rdfValue instanceof BNode) {
            builder.withQualifierSomeValue(property);
            return;
        } else if (!(rdfValue instanceof Resource)) {
            // TODO Invalid schema.
            return;
        }
        Resource rdfResource = (Resource) rdfValue;
        org.wikidata.wdtk.datamodel.interfaces.Value value =
                RdfToValue.get(type).getValue(statements, rdfResource, type);
        builder.withQualifierValue(storeProperty(property), value);
    }

    private void loadWasDerivedFrom(String propPrefix, Value rdfValue) {
        if (!(rdfValue instanceof Resource)) {
            // TODO Invalid schema.
            return;
        }
        RdfToReference rdfToReference =
                new RdfToReference(
                        register, entityPrefix, propPrefix, mergeStrategy);
        builder.withReference(rdfToReference.loadReference(
                (Resource) rdfValue, statements));
    }

    private void selectMergerStrategy(
            org.wikidata.wdtk.datamodel.interfaces.Statement statement) {
        mergeStrategy.put(
                statement,
                MergeStrategy.fromTypesOrDefault(types, MergeStrategy.MERGE));
    }

}
