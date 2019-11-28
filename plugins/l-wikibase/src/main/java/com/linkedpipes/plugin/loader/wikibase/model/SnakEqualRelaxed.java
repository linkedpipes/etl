package com.linkedpipes.plugin.loader.wikibase.model;

import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import java.math.BigDecimal;

class SnakEqualRelaxed implements SnakEqual {

    @Override
    public boolean equal(Value left, Value right) {
        // If object are not of the same type, then use default.
        if (!left.getClass().equals(right.getClass())) {
            return left.equals(right);
        }
        if (left instanceof QuantityValue) {
            return quantityValueEqual(
                    (QuantityValue) left, (QuantityValue) right);
        } else {
            // Else use default.
            return left.equals(right);
        }
    }

    private boolean quantityValueEqual(
            QuantityValue left, QuantityValue right) {

        return bigDecimalEqual(left.getLowerBound(), right.getLowerBound()) &&
                bigDecimalEqual(left.getUpperBound(), right.getUpperBound()) &&
                bigDecimalEqual(
                        left.getNumericValue(), right.getNumericValue()) &&
                left.getUnit().equals(right.getUnit()) &&
                left.getUnitItemId().equals(right.getUnitItemId());
    }

    private boolean bigDecimalEqual(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }

}
