package com.linkedpipes.plugin.loader.wikibase.model;

import org.wikidata.wdtk.datamodel.interfaces.NoValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.SomeValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class SnakMerger {

    private final SnakEqual snakEqualStrategy;

    public SnakMerger(SnakEqual snakEqualStrategy) {
        this.snakEqualStrategy = snakEqualStrategy;
    }

    public Map<PropertyIdValue, List<Value>> merge(
            List<SnakGroup> local, List<SnakGroup> remote) {
        Map<PropertyIdValue, List<Value>> result = new HashMap<>();

        local.forEach((group) -> {
            PropertyIdValue property = group.getProperty();
            List<Value> valuesForProperty = new LinkedList<>();
            result.put(property, valuesForProperty);
            group.getSnaks().forEach((snak) -> {
                addToList(valuesForProperty, snak);
            });
        });

        remote.forEach((group) -> {
            PropertyIdValue property =group.getProperty();
            List<Value> valuesForProperty;
            if (result.containsKey(property)) {
                valuesForProperty = result.get(property);
            } else {
                valuesForProperty = new LinkedList<>();
                result.put(property, valuesForProperty);
            }
            group.getSnaks().forEach((snak) -> {
                addToList(valuesForProperty, snak);
            });
        });
        return result;
    }

    /**
     * Add to a list if the item is not there using our custom
     * snak equal strategy, this allows relaxed comparison of values.
     */
    private void addToList(List<Value> list, Snak snak) {
        Value value = null;
        // Based on
        // https://wikidata.github.io/Wikidata-Toolkit/org/wikidata/wdtk/datamodel/interfaces/Snak.html
        if (snak instanceof NoValueSnak noValueSnak) {
            // Ignore this one.
            return;
        }
        if (snak instanceof SomeValueSnak someValueSnak) {
            // Ignore this one.
            return;
        }
        if (snak instanceof ValueSnak valueSnak) {
            // Ignore this one.
            value = valueSnak.getValue();
        }
        for (Value item : list) {
            if (snakEqualStrategy.equal(item, value)) {
                return;
            }
        }
        list.add(value);
    }

}
