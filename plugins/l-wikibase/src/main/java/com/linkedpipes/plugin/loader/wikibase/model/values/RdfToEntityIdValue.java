package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RdfToEntityIdValue implements ValueConverter<EntityIdValue> {

    private static Set<String> SUPPORTED = new HashSet<>(Arrays.asList(
            DatatypeIdValue.DT_ITEM,
            DatatypeIdValue.DT_PROPERTY,
            DatatypeIdValue.DT_LEXEME,
            DatatypeIdValue.DT_FORM,
            DatatypeIdValue.DT_SENSE
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
            case DatatypeIdValue.DT_ITEM:
                return Datamodel.makeItemIdValue(id, iri);
            case DatatypeIdValue.DT_PROPERTY:
                return Datamodel.makePropertyIdValue(id, iri);
            case DatatypeIdValue.DT_LEXEME:
                return Datamodel.makeLexemeIdValue(id, iri);
            case DatatypeIdValue.DT_FORM:
                return Datamodel.makeFormIdValue(id, iri);
            case DatatypeIdValue.DT_SENSE:
                return Datamodel.makeSenseIdValue(id, iri);
            default:
                return null;
        }
    }

    @Override
    public EntityIdValue getValue(
            Collection<Statement> statements, Resource resource) {
        throw new UnsupportedOperationException();
    }

}
