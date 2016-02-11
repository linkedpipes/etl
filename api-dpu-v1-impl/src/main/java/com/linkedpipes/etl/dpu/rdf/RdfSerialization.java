package com.linkedpipes.etl.dpu.rdf;

import com.linkedpipes.etl.dpu.api.rdf.RdfToPojo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * @author Škoda Petr
 */
public final class RdfSerialization {

    /**
     * Store types for primitive types wrappers and string.
     */
    private static final Set<Class<?>> WRAP_TYPES;

    public static class CanNotDeserializeObject extends Exception {

        public CanNotDeserializeObject(String message) {
            super(message);
        }

        public CanNotDeserializeObject(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Field type.
     */
    static enum FieldType {

        PRIMITIVE,
        COLLECTION,
        COMPLEX
    }

    static class FieldDescription {

        private FieldType type;

        private PropertyDescriptor property;

        private Field field;

        /**
         * Types that can be instantiated.
         */
        private List<Class<?>> types;

    }

    static class ObjectDescription {

        private String type;

        /**
         * Map of URI field,property descriptors.
         */
        private final Map<String, FieldDescription> uriToField = new HashMap<>();

    }

    private static final Logger LOG = LoggerFactory.getLogger(RdfSerialization.class);

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
        // ...
        WRAP_TYPES.add(String.class);
        WRAP_TYPES.add(Date.class);
    }

    /**
     * Auto detect entities, that can be loaded into given object.
     *
     * @param object
     * @param source
     * @param graph Graph to search entities for, if null is given default graph is used.
     * @throws com.linkedpipes.dpu.rdf.boundary.impl.RdfSerialization.CanNotDeserializeObject
     */
    public static void addToObject(Object object, SparqlSelect source, String graph) throws CanNotDeserializeObject {
        final String typeAsString = getObjectType(object);
        final String query;
        if (graph == null) {
            query = getQueryForTypes(typeAsString);
        } else {
            query = getQueryForTypes(typeAsString, graph);
        }
        try {
            for (Map<String, String> configPair : source.executeSelect(query)) {
                if (graph == null) {
                    loadIntoObject(object, source, configPair.get("s"), configPair.get("g"), new HashMap<>());
                } else {
                    loadIntoObject(object, source, configPair.get("s"), graph, new HashMap<>());
                }
            }
        } catch (SparqlSelect.QueryException ex) {
            throw new CanNotDeserializeObject("Query failure!", ex);
        }
    }

    /**
     * Load data from given entity and graph into given object.
     *
     * @param object
     * @param source
     * @param graph
     * @param resourceUri
     * @throws com.linkedpipes.dpu.rdf.boundary.impl.RdfSerialization.CanNotDeserializeObject
     */
    public static void addToObject(Object object, SparqlSelect source, String graph, String resourceUri
    ) throws CanNotDeserializeObject {
        LOG.info("addToObject graph: {} uri: {}", graph, resourceUri);
        try {
            loadIntoObject(object, source, resourceUri, graph, new HashMap<>());
        } catch (SparqlSelect.QueryException ex) {
            throw new CanNotDeserializeObject("Query failure!", ex);
        }
    }

    /**
     * Output binding:
     * <ul>
     * <li>s - resources URI.</li>
     * <li>g - graph name.</li>
     * </ul>
     *
     * @param type
     * @param graph If null GRAPHT statement is not used.
     * @return Query that search for objects of given types.
     */
    static String getQueryForTypes(String type) {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ?s ?g ");
        query.append("WHERE { GRAPH ?g {?s a <");
        query.append(type);
        query.append("> } }");
        return query.toString();
    }

    /**
     * Output binding:
     * <ul>
     * <li>s - resources URI.</li>
     * </ul>
     *
     * @param type
     * @param graph If null GRAPHT statement is not used.
     * @return Query that search for objects of given types.
     */
    static String getQueryForTypes(String type, String graph) {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ?s ?g ");
        query.append("FROM <");
        query.append(graph);
        query.append("> ");
        query.append("WHERE { ?s a <");
        query.append(type);
        query.append("> }");
        return query.toString();
    }

    /**
     * Output binding:
     * <ul>
     * <li>p - property URI.</li>
     * <li>o - property value.</li>
     * </ul>
     *
     * @param uri
     * @param graph
     * @return
     */
    static String getQueryProperties(String uri, String graph) {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ?p ?o FROM <");
        query.append(graph);
        query.append("> WHERE { <");
        query.append(uri);
        query.append("> ?p ?o }");
        return query.toString();
    }

    /**
     * Output binding:
     * <ul>
     * <li>type - resource type.</li>
     * </ul>
     *
     * @param uri
     * @param graph
     * @return
     */
    static String getQueryTypes(String uri, String graph) {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ?type FROM <");
        query.append(graph);
        query.append("> WHERE { <");
        query.append(uri);
        query.append("> a ?type }");
        return query.toString();
    }

    static String getObjectType(Object object) throws CanNotDeserializeObject {
        final RdfToPojo.Type type = object.getClass().getAnnotation(RdfToPojo.Type.class);
        if (type == null) {
            throw new CanNotDeserializeObject("Missing type annotation for: " + object.getClass().getName());
        }
        return type.uri();
    }

    /**
     * Load data from RDF into given object.
     *
     * @param object
     * @param source
     * @param resourceUri
     * @param graph
     * @param descriptionCache
     * @throws cz.cuni.mff.xrg.cuv.executor.api.v1.plugin.SparqlSelect.QueryException
     */
    static void loadIntoObject(Object object, SparqlSelect source, String resourceUri, String graph,
            Map<Class<?>, ObjectDescription> descriptionCache)
            throws SparqlSelect.QueryException, CanNotDeserializeObject {
        if (!descriptionCache.containsKey(object.getClass())) {
            descriptionCache.put(object.getClass(), createDescription(object.getClass()));
        }
        final String query = getQueryProperties(resourceUri, graph);
        final ObjectDescription objectDescription = descriptionCache.get(object.getClass());
        for (Map<String, String> item : source.executeSelect(query)) {
            if (objectDescription.uriToField.containsKey(item.get("p"))) {
                // There is binding for givne property, so bind it.
                final FieldDescription fieldDescription = objectDescription.uriToField.get(item.get("p"));
                final Object value;
                final String valueAsString = item.get("o");
                switch (fieldDescription.type) {
                    case PRIMITIVE:
                        value = loadPrimitiveType(fieldDescription.property.getPropertyType(), valueAsString);
                        break;
                    case COLLECTION:
                        loadCollection(fieldDescription, object, valueAsString, graph, source,
                                descriptionCache);
                        // And we can continue with next value! This skip the value == null check after the switch.
                        continue;
                    case COMPLEX:
                        value = loadIntoNewObject(fieldDescription.types, valueAsString, graph, source,
                                descriptionCache);
                        break;
                    default:
                        LOG.warn("Invalid type '{}' for field '{}'", fieldDescription.type,
                                fieldDescription.field.getName());
                        value = null;
                }
                if (value == null) {
                    throw new CanNotDeserializeObject("Can not load value for field: "
                            + fieldDescription.field.getName());
                }
                // Set field.
                try {
                    fieldDescription.property.getWriteMethod().invoke(object, value);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    throw new CanNotDeserializeObject("Can't set propety " + fieldDescription.field.getName()
                            + " during rdf deserialization to value: " + valueAsString, ex);
                }
            }
        }
    }

    static Object loadPrimitiveType(Class<?> clazz, String valueAsString) throws CanNotDeserializeObject {
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
                return Date.from(LocalDateTime.parse(valueAsString, DateTimeFormatter.ISO_DATE_TIME).
                        atZone(ZoneId.systemDefault()).toInstant());
            } else {
                return null;
            }
        } catch (RuntimeException ex) {
            throw new CanNotDeserializeObject("Can't deserialize RDF value: '" + valueAsString
                    + "' into class '" + clazz.getSimpleName() + "'", ex);
        }
    }

    /**
     * Try to create a new object from given resource. All given classes are tested.
     *
     * @param classes
     * @param valueAsString
     * @param graph
     * @param source
     * @param descriptionCache
     * @return
     * @throws cz.cuni.mff.xrg.cuv.component.simple.impl.controller.RdfSerialization.CanNotDeserializeObject
     * @throws cz.cuni.mff.xrg.cuv.executor.api.v1.plugin.SparqlSelect.QueryException
     */
    static Object loadIntoNewObject(Collection<Class<?>> classes, String valueAsString, String graph,
            SparqlSelect source, Map<Class<?>, ObjectDescription> descriptionCache)
            throws CanNotDeserializeObject, SparqlSelect.QueryException {
        for (Class<?> clazz : classes) {
            Object output = loadIntoNewObject(clazz, valueAsString, graph, source, descriptionCache);
            if (output != null) {
                return output;
            }
        }
        // No class can be used.
        return null;
    }

    /**
     * Try to load object of given class from the given resource.
     *
     * @param clazz
     * @param valueAsString
     * @param graph
     * @param source
     * @param descriptionCache
     * @return
     * @throws cz.cuni.mff.xrg.cuv.component.simple.impl.controller.RdfSerialization.CanNotDeserializeObject
     * @throws cz.cuni.mff.xrg.cuv.executor.api.v1.plugin.SparqlSelect.QueryException
     */
    static Object loadIntoNewObject(Class<?> clazz, String valueAsString, String graph,
            SparqlSelect source, Map<Class<?>, ObjectDescription> descriptionCache)
            throws CanNotDeserializeObject, SparqlSelect.QueryException {
        // Get description.
        if (!descriptionCache.containsKey(clazz)) {
            descriptionCache.put(clazz, createDescription(clazz));
        }
        final ObjectDescription objectDescriotion = descriptionCache.get(clazz);
        // Check type.
        boolean typeMatch = false;
        final String query = getQueryTypes(valueAsString, graph);
        for (Map<String, String> item : source.executeSelect(query)) {
            if (objectDescriotion.type.equals(item.get("type"))) {
                typeMatch = true;
                break;
            }
        }
        if (!typeMatch) {
            LOG.debug("Type does not match!");
            return null;
        }
        // Load.
        try {
            final Object result = clazz.newInstance();
            loadIntoObject(result, source, valueAsString, graph, descriptionCache);
            return result;
        } catch (CanNotDeserializeObject | IllegalAccessException | InstantiationException ex) {
            throw new CanNotDeserializeObject("Can't create instance of object.", ex);
        }
//        {
//            // Ctor from string - literal, etc ..
//            try {
//                final Constructor<?> ctr = classes.getConstructor(String.class);
//                result = ctr.newInstance(valueAsString);
//            } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
//                    IllegalArgumentException | InstantiationException | InvocationTargetException ex) {
//                throw new CanNotDeserializeObject("Can't create instance of object, from ctor(String).", ex);
//            }
//        }
    }

    static void loadCollection(FieldDescription description, Object object, String valueAsString, String graph,
            SparqlSelect source, Map<Class<?>, ObjectDescription> descriptionCache)
            throws CanNotDeserializeObject, SparqlSelect.QueryException {
        // Get read method as we need to call add method on given collection.
        final Method readMethod = description.property.getReadMethod();
        final Collection collection;
        try {
            collection = (Collection) readMethod.invoke(object);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new CanNotDeserializeObject("Can't read value of collection: "
                    + description.field.getName(), ex);
        }
        if (collection == null) {
            throw new CanNotDeserializeObject("Collection must be initialized prio to loading. Collection: "
                    + description.field.getName() + " on class: " + object.getClass().getCanonicalName());
        }
        // try conversion again
        Object value = null;
        if (description.types.size() == 1) {
            // We can try to load as a primitive type.
            value = loadPrimitiveType(description.types.get(0), valueAsString);
        }
        if (value != null) {
            // It's primitive in collection, so just add the value.
            collection.add(value);
        } else {
            // Complex type.
            value = loadIntoNewObject(description.types, valueAsString, graph, source, descriptionCache);
            if (value != null) {
                // The object has been loaded, add it to the collection.
                collection.add(value);
            }
        }
    }

    /**
     * Return generic type of a collection.
     *
     * @param type
     * @return
     */
    static Class<?> getCollectionGenericType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            LOG.warn("Superclass it not ParameterizedType");
            return null;
        }
        final Type[] params = ((ParameterizedType) type).getActualTypeArguments();
        // We know there should be just one for Collection.
        if (params.length != 1) {
            LOG.warn("Unexpected number of generic types: {} (1 expected)", params.length);
            return null;
        }
        if (!(params[0] instanceof Class)) {
            LOG.warn("Unexpected type '{}'", params[0].toString());
            return null;
        }
        return (Class<?>) params[0];
    }

    static ObjectDescription createDescription(Class<?> clazz) throws CanNotDeserializeObject {
        final ObjectDescription description = new ObjectDescription();
        // Search for type.
        final RdfToPojo.Type type = clazz.getAnnotation(RdfToPojo.Type.class);
        if (type == null) {
            throw new CanNotDeserializeObject("Missing type annotation for: " + clazz.getSimpleName());
        } else {
            description.type = type.uri();
        }
        // Not annotated fields - properties.
        final List<Field> fields = new ArrayList<>(12);

        // Iterate over super classes and gather fiel
        Class<?> currentClass = clazz;
        do {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null);

        for (Field field : fields) {
            final Class<?> fieldClass = field.getType();
            final RdfToPojo.Property property = field.getAnnotation(RdfToPojo.Property.class);
            if (property == null) {
                continue;
            }
            final FieldDescription fieldDescription = new FieldDescription();
            // We can set field.
            fieldDescription.field = field;
            // Now we need property description.
            try {
                fieldDescription.property = new PropertyDescriptor(field.getName(), clazz);
            } catch (IntrospectionException ex) {
                throw new CanNotDeserializeObject("Can't get property descriptor for: " + field.getName()
                        + " and class: " + clazz.getSimpleName() + " (missing getter/setter?).", ex);
            }
            // We check for multpile types.
            final RdfToPojo.Types instaces = field.getAnnotation(RdfToPojo.Types.class);
            if (instaces == null) {
                // It's not set!
                fieldDescription.types = null;
            } else {
                fieldDescription.types = Arrays.asList(instaces.classes());
            }
            // And at the end we need to determine type.
            if (Collection.class.isAssignableFrom(fieldClass)) {
                fieldDescription.type = FieldType.COLLECTION;
                if (fieldDescription.types == null) {
                    final Class<?> innerClass = getCollectionGenericType(field.getGenericType());
                    if (innerClass == null) {
                        throw new CanNotDeserializeObject("Can't get type of Collection for: "
                                + field.getName());
                    }
                    fieldDescription.types = Arrays.asList(innerClass);
                }
            } else if (fieldClass.isPrimitive() || WRAP_TYPES.contains(fieldClass)) {
                fieldDescription.type = FieldType.PRIMITIVE;
            } else if (fieldClass.isArray() || fieldClass.isEnum()) {
                throw new CanNotDeserializeObject("Given type is not supported: " + clazz.getSimpleName());
            } else {
                fieldDescription.type = FieldType.COMPLEX;
                if (fieldDescription.types == null) {
                    fieldDescription.types = Arrays.asList(field.getType());
                }
            }
            description.uriToField.put(property.uri(), fieldDescription);
        }

        return description;
    }

}
