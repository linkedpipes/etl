package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.rdf.Vocabulary;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RdfToEntityIdValue implements ValueConverter<EntityIdValue> {

    private final static Set<String> SUPPORTED = new HashSet<>(List.of(
            Vocabulary.DT_ITEM,
            Vocabulary.DT_PROPERTY,
            Vocabulary.DT_LEXEME,
            Vocabulary.DT_FORM,
            Vocabulary.DT_SENSE
    ));

    @Override
    public Set<String> getSupportedTypes() {
        return SUPPORTED;
    }

    @Override
    public EntityIdValue getValue(Value value, String type) {
        String valueStr = value.stringValue();
        String iri = valueStr.substring(0, valueStr.lastIndexOf("/") + 1);
        String id = valueStr.substring(valueStr.lastIndexOf("/") + 1);
        switch (type) {
            case Vocabulary.DT_ITEM:
                return Datamodel.makeItemIdValue(id, iri);
            case Vocabulary.DT_PROPERTY:
                return Datamodel.makePropertyIdValue(id, iri);
            case Vocabulary.DT_LEXEME:
                return Datamodel.makeLexemeIdValue(id, iri);
            case Vocabulary.DT_FORM:
                return Datamodel.makeFormIdValue(id, iri);
            case Vocabulary.DT_SENSE:
                return Datamodel.makeSenseIdValue(id, iri);
            default:
                return null;
        }
    }

    @Override
    public EntityIdValue getValue(
            Collection<Statement> statements, Resource resource, String type) {
        throw new UnsupportedOperationException();
    }

}
