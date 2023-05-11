package com.linkedpipes.plugin.loader.wikibase.model;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.Value;

public interface SnakEqual {

    boolean equal(Value left, Value right);

    default boolean equal(Snak left, Snak right) {
        if (Objects.equal(left.getPropertyId(), right.getPropertyId())) {
            return false;
        }
        // TODO We should check for values based on snak instance:
        // https://wikidata.github.io/Wikidata-Toolkit/org/wikidata/wdtk/datamodel/interfaces/Snak.html
        return Objects.equal(left, right);
    }

    static SnakEqual strict() {
        return Object::equals;
    }

    static SnakEqual relaxed() {
        return new SnakEqualRelaxed();
    }

}
