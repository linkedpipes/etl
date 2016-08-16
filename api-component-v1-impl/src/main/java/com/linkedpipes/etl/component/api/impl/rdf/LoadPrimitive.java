package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Petr Škoda
 */
class LoadPrimitive extends LoaderToValue {

    private static final Logger LOG
            = LoggerFactory.getLogger(LoadPrimitive.class);

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

    static Object loadPrimitive(Class<?> clazz, String valueAsString)
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
                // We expect XSD date yyyy-MM-dd
                final DateFormat dateFormat =
                        new SimpleDateFormat("yyyy-MM-dd");
                // We use GMT time zone as default, to have the same
                // settings on different systems.
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                return dateFormat.parse(valueAsString);
            } else {
                return null;
            }
        } catch (Exception ex) {
            LOG.info("Can't parse value: {}", valueAsString, ex);
            throw new CanNotDeserializeObject(
                    "Can't deserialize RDF value: '"
                            + valueAsString + "' into class '"
                            + clazz.getSimpleName() + "'", ex);
        }
    }

}
