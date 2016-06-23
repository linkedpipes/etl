package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Petr Škoda
 */
class LoadPrimitive extends LoaderToValue {

    LoadPrimitive(PropertyDescriptor property, Field field) {
        super(property, field);
    }

    @Override
    public void load(Object object, Map<String, String> property,
            String graph, SparqlSelect select) throws CanNotDeserializeObject {
        final Object value = loadPrimitive(field.getType(),
                property.get("value"));
        set(object, value, property.get("value"));
    }

    private static Object loadPrimitive(Class<?> clazz, String valueAsString)
            throws CanNotDeserializeObject {
        // TODO Add null check for all values.
        try {
            if (clazz == String.class) {
                return valueAsString;
            } else if (clazz == boolean.class || clazz == Boolean.class) {
                return Boolean.parseBoolean(valueAsString);
            } else if (clazz == byte.class || clazz == Byte.class) {
                return Byte.parseByte(valueAsString);
            } else if (clazz == short.class || clazz == Short.class) {
                return Short.parseShort(valueAsString);
            } else if (clazz == int.class || clazz == Integer.class) {
                return Integer.parseInt(valueAsString);
            } else if (clazz == long.class || clazz == Long.class) {
                return Long.parseLong(valueAsString);
            } else if (clazz == float.class || clazz == Float.class) {
                return Float.parseFloat(valueAsString);
            } else if (clazz == double.class || clazz == Double.class) {
                return Double.parseDouble(valueAsString);
            } else if (clazz == Date.class) {
                return Date.from(LocalDateTime.parse(valueAsString,
                        DateTimeFormatter.ISO_DATE_TIME).
                        atZone(ZoneId.systemDefault()).toInstant());
            } else {
                return null;
            }
        } catch (RuntimeException ex) {
            throw new CanNotDeserializeObject(
                    "Can't deserialize RDF value: '"
                    + valueAsString + "' into class '"
                    + clazz.getSimpleName() + "'", ex);
        }
    }

}
