package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.plugin.loader.wikibase.model.values.RdfToEntityIdValue;
import com.linkedpipes.plugin.loader.wikibase.model.values.RdfToGlobeCoordinatesValue;
import com.linkedpipes.plugin.loader.wikibase.model.values.RdfToMonolingualTextValue;
import com.linkedpipes.plugin.loader.wikibase.model.values.RdfToQuantityValue;
import com.linkedpipes.plugin.loader.wikibase.model.values.RdfToStringValue;
import com.linkedpipes.plugin.loader.wikibase.model.values.RdfToTimeValue;
import com.linkedpipes.plugin.loader.wikibase.model.values.ValueConverter;

import java.util.HashMap;
import java.util.Map;

class RdfToValue {

    private static Map<String, ValueConverter> VALUE_CONVERTERS =
            new HashMap<>();

    static {
        (new RdfToEntityIdValue()).register(VALUE_CONVERTERS);
        (new RdfToGlobeCoordinatesValue()).register(VALUE_CONVERTERS);
        (new RdfToMonolingualTextValue()).register(VALUE_CONVERTERS);
        (new RdfToQuantityValue()).register(VALUE_CONVERTERS);
        (new RdfToStringValue()).register(VALUE_CONVERTERS);
        (new RdfToTimeValue()).register(VALUE_CONVERTERS);
    }

    public static ValueConverter get(String type) {
        return VALUE_CONVERTERS.get(type);
    }

}
