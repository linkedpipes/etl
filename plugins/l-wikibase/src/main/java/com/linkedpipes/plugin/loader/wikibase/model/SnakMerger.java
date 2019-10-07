package com.linkedpipes.plugin.loader.wikibase.model;

import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class SnakMerger {

    private SnakEqual snakEqualStrategy;

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
                addToList(valuesForProperty, snak.getValue());
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
                addToList(valuesForProperty, snak.getValue());
            });
        });

        return result;
    }

    /**
     * Add to a list if the item is not there using our custom
     * snak equal strategy, this allow to relaxed comparision of values.
     */
    private void addToList(List<Value> list, Value value) {
        for (Value item : list) {
            if (snakEqualStrategy.equal(item, value)) {
                return;
            }
        }
        list.add(value);
    }

}
