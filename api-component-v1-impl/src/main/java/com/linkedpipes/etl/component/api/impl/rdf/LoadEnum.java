package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Map;

/**
 *
 * @author Petr Škoda
 */
class LoadEnum extends LoaderToValue {

    LoadEnum(PropertyDescriptor property, Field field) {
        super(property, field);
    }

    @Override
    public void load(Object object, Map<String, String> property,
            String graph, SparqlSelect select) throws CanNotDeserializeObject {
        final Class<? extends Enum> clazzEnum
                = (Class<? extends Enum>) field.getType();
        final Object value = Enum.valueOf(clazzEnum, property.get("value"));
        // Set value.
        set(object, value, property.get("value"));
    }

}
