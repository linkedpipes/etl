package com.linkedpipes.plugin.loader.wikibase.model;

import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SnakMerger {

    public Map<PropertyIdValue, Set<Value>> merge(
            List<SnakGroup> local, List<SnakGroup> remote) {
        Map<PropertyIdValue, Set<Value>> result = new HashMap<>();

        local.forEach((group) -> {
            PropertyIdValue property = group.getProperty();
            Set<Value> valuesForProperty = new LinkedHashSet<>();
            result.put(property, valuesForProperty);
            group.getSnaks().forEach((snak) -> {
                valuesForProperty.add(snak.getValue());
            });
        });

        remote.forEach((group) -> {
            PropertyIdValue property =group.getProperty();
            Set<Value> valuesForProperty;
            if (result.containsKey(property)) {
                valuesForProperty = result.get(property);
            } else {
                valuesForProperty = new LinkedHashSet<>();
                result.put(property, valuesForProperty);
            }
            group.getSnaks().forEach((snak) -> {
                valuesForProperty.add(snak.getValue());
            });
        });

        return result;
    }

}
