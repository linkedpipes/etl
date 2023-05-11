package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.rdf.RdfWriter;
import org.wikidata.wdtk.rdf.Vocabulary;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RdfToQuantityValue implements ValueConverter<QuantityValue> {

    private final static Set<String> SUPPORTED = new HashSet<>(List.of(
            Vocabulary.DT_QUANTITY
    ));

    @Override
    public Set<String> getSupportedTypes() {
        return SUPPORTED;
    }

    @Override
    public QuantityValue getValue(Value value, String type) {
        if (value instanceof Literal) {
            Literal literal = (Literal) value;
            return Datamodel.makeQuantityValue(
                    literal.longValue(),
                    literal.longValue(),
                    literal.longValue());
        } else {
            // org.wikidata.wdtk.rdf.Vocabulary.getQuantityValueUri
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public QuantityValue getValue(
            Collection<Statement> statements, Resource resource, String type) {
        BigDecimal value = BigDecimal.ZERO;
        BigDecimal lowerBound = null;
        BigDecimal upperBound = null;
        ItemIdValue unit = null;
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            IRI predicate = statement.getPredicate();
            Value object = statement.getObject();
            if (RdfWriter.WB_QUANTITY_AMOUNT.equals(predicate)) {
                value = new BigDecimal(object.stringValue());
            } else if (RdfWriter.WB_QUANTITY_LOWER_BOUND.equals(predicate)) {
                lowerBound = new BigDecimal(object.stringValue());
            } else if (RdfWriter.WB_QUANTITY_UPPER_BOUND.equals(predicate)) {
                upperBound = new BigDecimal(object.stringValue());
            } else if (RdfWriter.WB_QUANTITY_UNIT.equals(predicate)) {
                if (Vocabulary.WB_NO_UNIT.equals(object.stringValue())) {
                    // Thera was 1 before, now we employ null for no unit.
                    unit = null;
                } else {
                    // We need to split the string using '/'.
                    String unitAsString = object.stringValue();
                    int separator = unitAsString.lastIndexOf('/') + 1;
                    try {
                        unit = Datamodel.makeItemIdValue(
                                unitAsString.substring(separator),
                                unitAsString.substring(0, separator));
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException(
                                "Invalid Wikibase entity IRI: "
                                        + unitAsString, ex);
                    }
                }
            }
        }
        return Datamodel.makeQuantityValue(value, lowerBound, upperBound, unit);
    }

}
