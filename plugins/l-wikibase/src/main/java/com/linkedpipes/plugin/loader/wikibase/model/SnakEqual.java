package com.linkedpipes.plugin.loader.wikibase.model;

import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.Value;

public interface SnakEqual {

    boolean equal(Value left, Value right);

    default boolean equal(Snak left, Snak right) {
        return left.getPropertyId().equals(right.getPropertyId()) &&
                equal(left.getValue(), right.getValue());
    }

    static SnakEqual strict() {
        return new SnakEqual() {
            @Override
            public boolean equal(Value left, Value right) {
                return left.equals(right);
            }
        };
    }

    static SnakEqual relaxed() {
        return new SnakEqualRelaxed();
    }

}
