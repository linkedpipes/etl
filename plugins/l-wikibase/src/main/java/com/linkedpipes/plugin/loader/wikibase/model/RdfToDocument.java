package com.linkedpipes.plugin.loader.wikibase.model;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.rdf.PropertyRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Does not support:
 * * site links
 */
public class RdfToDocument {

    private ItemDocumentBuilder builder;

    private List<String> types;

    private Collection<Statement> statements;

    private ItemIdValue documentId;

    private final PropertyRegister register;

    private final String entityPrefix;

    private final String propPrefix;

    /**
     * We need to store merge information (type) of entities,
     * this can not be saved within the wikidata entries, so instead
     * we carry around this object and store the information there.
     */
    private Map<Object, MergeStrategy> mergeStrategy;

    public RdfToDocument(PropertyRegister register, String siteUrl) {
        this.register = register;
        this.entityPrefix = siteUrl + "entity/";
        this.propPrefix = siteUrl + "prop/";
    }

    public ItemDocument loadItemDocument(
            Collection<Statement> statements,
            String iriAsStr) {
        return loadItemDocument(
                statements,
                SimpleValueFactory.getInstance().createIRI(iriAsStr));
    }

    public ItemDocument loadItemDocument(
            Collection<Statement> statements,
            Resource resource) {
        types = new ArrayList<>();
        documentId = resourceToItemId(resource);
        builder = ItemDocumentBuilder.forItemId(documentId);
        mergeStrategy = new HashMap<>();
        this.statements = statements;

        statements.stream()
                .filter((st) -> st.getSubject().equals(resource))
                .forEach((st) -> {
                    IRI predicate = st.getPredicate();
                    if (predicate.equals(RDF.TYPE)) {
                        types.add(st.getObject().stringValue());
                    } else if (isLabelPredicate(predicate)) {
                        handleLabel(st.getObject());
                    } else if (isDescriptionPredicate(predicate)) {
                        handleDescription(st.getObject());
                    } else if (predicate.stringValue().startsWith(propPrefix)) {
                        PropertyIdValue property =
                                iriToPropertyId(st.getPredicate());
                        handlePredicate(property, st.getObject());
                    }
                });

        checkForNoValue();
        ItemDocument results = builder.build();
        selectMergerStrategy(results);
        return results;
    }

    private ItemIdValue resourceToItemId(Resource resource) {
        String strValue = resource.stringValue();
        if (strValue.lastIndexOf("/") == -1) {
            return ItemIdValue.NULL;
        }
        String id = strValue.substring(strValue.lastIndexOf("/") + 1);
        String siteIri = strValue.substring(0, strValue.lastIndexOf("/") + 1);
        return Datamodel.makeItemIdValue(id, siteIri);
    }

    private boolean isLabelPredicate(IRI predicate) {
        String predicateAsStr = predicate.stringValue();
        return "http://schema.org/name".equals(predicateAsStr);
    }

    private void handleLabel(Value value) {
        Literal literal = (Literal) value;
        builder.withLabel(literal.getLabel(), literal.getLanguage().get());
    }

    private boolean isDescriptionPredicate(IRI predicate) {
        String predicateAsStr = predicate.stringValue();
        return "http://schema.org/description".equals(predicateAsStr);
    }

    private void handleDescription(Value value) {
        Literal literal = (Literal) value;
        builder.withDescription(literal.getLabel(), literal.getLanguage().get());
    }

    private PropertyIdValue iriToPropertyId(IRI iri) {
        String strValue = iri.stringValue();
        String id = strValue.substring(strValue.lastIndexOf("/") + 1);
        return Datamodel.makePropertyIdValue(id, propPrefix);
    }

    private void handlePredicate(PropertyIdValue property, Value value) {
        if (register.getPropertyType(property) == null) {
            return;
        }
        if (value instanceof Resource) {
            loadStatement((Resource) value, property);
        } else {
            // wikibase:Item wdt:P3 "some value"
            // TODO Should we supported statement shortcuts?
        }
    }

    private void loadStatement(
            Resource resource, PropertyIdValue property) {
        RdfToStatement loader =
                new RdfToStatement(
                        register, entityPrefix, propPrefix, mergeStrategy);
        org.wikidata.wdtk.datamodel.interfaces.Statement statement =
                loader.loadStatement(
                        documentId, property, resource, statements);

        if (statement != null) {
            builder.withStatement(statement);
        }
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
            builder.withStatement(
                    StatementBuilder
                            .forSubjectAndProperty(documentId, property)
                            .build());
        }
    }

    private void selectMergerStrategy(ItemDocument document) {
        mergeStrategy.put(document, MergeStrategy.fromTypes(types));
    }

    public Map<Object, MergeStrategy> getMergeStrategyForLastLoadedDocument() {
        return mergeStrategy;
    }

}
