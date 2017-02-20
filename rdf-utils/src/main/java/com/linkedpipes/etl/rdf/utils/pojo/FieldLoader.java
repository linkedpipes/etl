package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Can be used to load values into a properties of an object.
 *
 * Known limitations:
 * <ul>
 * <li>Does not support arrays.</li>
 * <li>Does not support generics with more then one argument.</li>
 * <li>Does not support nested collections.</li>
 * <li>Does not support language tags.</li>
 * </ul>
 *
 * This class can be extended to provide additional functionality.
 */
class FieldLoader<ValueType> {

    public static class CantLoadValue extends Exception {

        public CantLoadValue(String message) {
            super(message);
        }

        public CantLoadValue(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final Set<Class<?>> WRAP_TYPES;

    private static final Logger LOG =
            LoggerFactory.getLogger(FieldLoader.class);

    private static final DateFormat XSD_DATE_FORMAT;

    static {
        WRAP_TYPES = new HashSet<>();
        WRAP_TYPES.add(Boolean.class);
        WRAP_TYPES.add(Character.class);
        WRAP_TYPES.add(Byte.class);
        WRAP_TYPES.add(Short.class);
        WRAP_TYPES.add(Integer.class);
        WRAP_TYPES.add(Long.class);
        WRAP_TYPES.add(Float.class);
        WRAP_TYPES.add(Double.class);
        WRAP_TYPES.add(Void.class);
        WRAP_TYPES.add(String.class);
        WRAP_TYPES.add(Date.class);
        // We use GMT time zone as default, to have the same
        // settings on different systems.
        XSD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        XSD_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final RdfSource.ValueConverter<ValueType> converter;

    public FieldLoader(RdfSource.ValueConverter<ValueType> converter) {
        this.converter = converter;
    }

    /**
     * If the field represent a primitive type, then the value is converted
     * to given type and stored in the field.
     *
     * If the field represent a complex object or a collection,
     * then the behaviour depends on extendExisting.
     *
     * If the extendExisting is true, then new object is added to collection,
     * and new object is set as a complex type.
     *
     * If the extendExisting is false, the collection is cleared before adding
     * any object and new complex objects are created.
     *
     * @param target
     * @param field
     * @param value
     * @param extendExisting
     * @return
     */
    public Object set(Object target, Field field, ValueType value,
            boolean extendExisting)
            throws CantLoadValue {
        final Class<?> fieldType = field.getType();
        if (Collection.class.isAssignableFrom(fieldType)) {
            final Class<?> genericType =
                    getCollectionType(field.getGenericType());
            if (Collection.class.isAssignableFrom(genericType)) {
                throw new CantLoadValue("Nested collection are not supported.");
            } else if (isPrimitive(genericType)) {
                addValue(target, field,
                        valueToPrimitive(genericType, value), extendExisting);
            } else if (genericType.isEnum()) {
                addValue(target, field, valueToEnum(genericType, value),
                        extendExisting);
            } else if (genericType.isArray()) {
                throw new CantLoadValue("Arrays are not supported.");
            } else {
                if (RdfLoader.LangString.class.isAssignableFrom(genericType)) {
                    addValue(target, field, valueToStringLang(genericType, value),
                            extendExisting);
                    return null;
                } else {
                    final Object newObject = valueToObject(genericType);
                    addValue(target, field, newObject, extendExisting);
                    return newObject;
                }
            }
        } else if (isPrimitive(fieldType)) {
            setValue(target, field, valueToPrimitive(fieldType, value));
        } else if (fieldType.isEnum()) {
            setValue(target, field, valueToEnum(fieldType, value));
        } else if (fieldType.isArray()) {
            throw new CantLoadValue("Arrays are not supported.");
        } else {
            if (RdfLoader.LangString.class.isAssignableFrom(fieldType)) {
                setValue(target, field, valueToStringLang(fieldType, value));
                return null;
            }
            // It's a regular object.
            if (extendExisting) {
                final Object currentObject = getValue(target, field);
                if (currentObject != null) {
                    return currentObject;
                }
            }
            // Create and set new object.
            final Object newObject = valueToObject(fieldType);
            setValue(target, field, newObject);
            return newObject;
        }
        return null;
    }

    private static Object getValue(Object object, Field field)
            throws CantLoadValue {
        final PropertyDescriptor descriptor;
        try {
            descriptor = new PropertyDescriptor(field.getName(),
                    field.getDeclaringClass());
        } catch (IntrospectionException ex) {
            throw new CantLoadValue("Can't handle property descriptor.");
        }
        try {
            return descriptor.getReadMethod().invoke(object);
        } catch (Throwable ex) {
            throw new CantLoadValue("Can't set value", ex);
        }
    }

    /**
     * Set given value to given field.
     *
     * @param object
     * @param field
     * @param value
     */
    private static void setValue(Object object, Field field, Object value)
            throws CantLoadValue {
        final PropertyDescriptor descriptor;
        try {
            descriptor = new PropertyDescriptor(field.getName(),
                    field.getDeclaringClass());
        } catch (IntrospectionException ex) {
            throw new CantLoadValue("Can't handle property descriptor.");
        }
        try {
            descriptor.getWriteMethod().invoke(object, value);
        } catch (Throwable ex) {
            throw new CantLoadValue("Can't set value", ex);
        }
    }

    /**
     * Add given value to a collection.
     *
     * @param object
     * @param field
     * @param value
     * @param extendExisting
     */
    private static void addValue(Object object, Field field, Object value,
            boolean extendExisting) throws CantLoadValue {
        final PropertyDescriptor descriptor;
        try {
            descriptor = new PropertyDescriptor(field.getName(),
                    field.getDeclaringClass());
        } catch (IntrospectionException ex) {
            throw new CantLoadValue("Can't handle property descriptor.");
        }
        // Read collection.
        final Collection collection;
        try {
            collection = (Collection) descriptor.getReadMethod().invoke(object);
        } catch (Exception ex) {
            throw new CantLoadValue(
                    "Can't readByReflection value of collection: "
                            + field.getName(), ex);
        }
        if (collection == null) {
            throw new CantLoadValue(
                    "Collection must be initialized prior to loading."
                            + " Collection: '" + field.getName()
                            + "' on class: '" +
                            object.getClass().getCanonicalName()
                            + "'");
        }
        // Add value.
        if (!extendExisting) {
            collection.clear();
        }
        collection.add(value);
    }

    /**
     * Create new object of given type.
     *
     * @param fieldType
     * @return
     */
    private Object valueToObject(Class<?> fieldType) throws CantLoadValue {
        // We need to check if the value represent a literal or
        // a resource.
        final Object newObject;
        try {
            newObject = fieldType.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new CantLoadValue("Can't handle object instance.", ex);
        }
        return newObject;
    }

    private Object valueToEnum(Class<?> fieldType, ValueType value) {
        return Enum.valueOf((Class<Enum>) fieldType, converter.asString(value));
    }

    /**
     * Create and return new object that represent a string with language tag.
     *
     * @param fieldType Type must be assignable to RdfLoader.LangString.
     * @param value
     * @return
     */
    private Object valueToStringLang(Class<?> fieldType, ValueType value)
            throws CantLoadValue {
        final RdfLoader.LangString object =
                (RdfLoader.LangString) valueToObject(fieldType);
        object.setValue(converter.asString(value),
                converter.langTag(value));
        return object;
    }

    /**
     * Convert given value to primitive of required type.
     *
     * @param fieldType
     * @param value
     * @return
     */
    private Object valueToPrimitive(Class<?> fieldType, ValueType value)
            throws CantLoadValue {
        try {
            if (fieldType == String.class) {
                return converter.asString(value);
            } else if (fieldType == boolean.class ||
                    fieldType == Boolean.class) {
                return converter.asBoolean(value);
            } else if (fieldType == byte.class || fieldType == Byte.class) {
                return converter.asInteger(value);
            } else if (fieldType == short.class || fieldType == Short.class) {
                return converter.asInteger(value);
            } else if (fieldType == int.class || fieldType == Integer.class) {
                return converter.asInteger(value);
            } else if (fieldType == long.class || fieldType == Long.class) {
                return converter.asLong(value);
            } else if (fieldType == float.class || fieldType == Float.class) {
                return converter.asFloat(value);
            } else if (fieldType == double.class || fieldType == Double.class) {
                return converter.asDouble(value);
            } else if (fieldType == Date.class) {
                return XSD_DATE_FORMAT.parse(converter.asString(value));
            } else {
                LOG.error("Unknown property type: {}", fieldType);
                return null;
            }
        } catch (Exception ex) {
            throw new CantLoadValue("Can't convert to primitive.", ex);
        }
    }

    /**
     * @param fieldClass
     * @return True if the class represent a primitive type.
     */
    private static boolean isPrimitive(Class<?> fieldClass) {
        return fieldClass.isPrimitive() || WRAP_TYPES.contains(fieldClass);
    }

    /**
     * Return generic type of a collection.
     *
     * @param type
     * @return
     */
    private static Class<?> getCollectionType(Type type) throws CantLoadValue {
        if (!(type instanceof ParameterizedType)) {
            LOG.warn("Superclass it not instance of ParameterizedType");
            return null;
        }
        final Type[] params =
                ((ParameterizedType) type).getActualTypeArguments();
        // We know there should be just one for Collection.
        if (params.length != 1) {
            throw new CantLoadValue("Unexpected number of generic types: "
                    + params.length + " (1 expected)");
        }
        if (!(params[0] instanceof Class)) {
            throw new CantLoadValue("Unexpected type: " + params[0].toString());
        }
        return (Class<?>) params[0];
    }

}
