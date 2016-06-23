package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Petr Škoda
 */
class LoadCollection extends LoaderToValue {

    private final List<Class<?>> types;

    LoadCollection(List<Class<?>> types, PropertyDescriptor property,
            Field field) {
        super(property, field);
        this.types = types;
    }

    @Override
    public void load(Object object, Map<String, String> property, String graph,
            SparqlSelect select) throws CanNotDeserializeObject {
        // Get colllection.
        final Method readMethod = this.property.getReadMethod();
        final Collection collection;
        try {
            collection = (Collection) readMethod.invoke(object);
        } catch (Exception ex) {
            throw new CanNotDeserializeObject("Can't read value of collection: "
                    + field.getName(), ex);
        }
        if (collection == null) {
            throw new CanNotDeserializeObject(
                    "Collection must be initialized prior to loading."
                    + " Collection: '" + field.getName()
                    + "' on class: '" + object.getClass().getCanonicalName()
                    + "'");
        }
        // Load value.
        for (Class<?> type : types) {
            Object value;
            try {
                value = LoadObject.loadNew(type, property.get("value"),
                        graph, select);
            } catch (CanNotDeserializeObject ex) {
                continue;
            }
            if (value != null) {
                // We got value!
                collection.add(value);
                break;
            }
        }
    }

}
