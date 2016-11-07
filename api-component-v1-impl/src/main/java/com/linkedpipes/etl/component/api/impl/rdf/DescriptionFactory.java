package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.component.api.service.RdfToPojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Create a description (for java object, field) that can be used to load
 * value into the given object, field.
 *
 * Is used in process of loading RDF into Java objects.
 */
class DescriptionFactory {

    private static final Logger LOG
            = LoggerFactory.getLogger(DescriptionFactory.class);

    private static final Set<Class<?>> WRAP_TYPES;

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
    }

    public Map<String, List<Loader>> createDescription(Class<?> type,
            RdfReader.MergeOptionsFactory optionsFactory)
            throws Loader.CanNotDeserializeObject {
        final Map<String, List<Loader>> result = new HashMap<>();

        // List of all field.
        final List<Field> fields = listFields(type);

        // Check fields.
        for (Field field : fields) {
            // Get basic information about field.
            final Class<?> fieldType = field.getType();
            final RdfToPojo.Property property = field.getAnnotation(
                    RdfToPojo.Property.class);
            if (property == null) {
                continue;
            }
            final PropertyDescriptor descriptor
                    = getPropertyDescriptor(field, type);
            // Check type.
            if (Collection.class.isAssignableFrom(fieldType)) {
                final Class<?> collectionType
                        = getCollectionGenericType(field.getGenericType());
                if (collectionType == null) {
                    throw new Loader.CanNotDeserializeObject(
                            "Can't get type of Collection for: '"
                                    + field.getName() + "'");
                }
                append(result, property.uri(),
                        new LoadCollection(Arrays.asList(collectionType),
                                descriptor, field, optionsFactory));
            } else if (isPrimitive(fieldType)) {
                append(result, property.uri(),
                        new LoadPrimitive(descriptor, field));
            } else if (fieldType.isEnum()) {
                append(result, property.uri(),
                        new LoadEnum(descriptor, field));
            } else if (fieldType.isArray()) {
                throw new Loader.CanNotDeserializeObject(
                        "Array type is not supported: " + type.getName());
            } else {
                // Complex type.
                if (fieldType.getAnnotation(RdfToPojo.Type.class) != null) {
                    // Complex type.
                    append(result, property.uri(),
                            new LoadObject(descriptor, field, optionsFactory));
                }
                if (fieldType.getAnnotation(RdfToPojo.Value.class) != null) {
                    // Expanded literal.
                    append(result, property.uri(),
                            createLiteralDescription(descriptor, field));
                }
            }
        }
        return result;
    }

    /**
     * List all fields in class and ancestors.
     *
     * @param type
     * @return
     */
    private static List<Field> listFields(Class<?> type) {
        // List of all field.
        final List<Field> fields = new ArrayList<>(12);
        Class<?> currentClass = type;
        do {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null);
        return fields;
    }

    private LoadLiteral createLiteralDescription(
            PropertyDescriptor targetDescriptor, Field targetField)
            throws Loader.CanNotDeserializeObject {
        final LoadLiteral descriptor =
                createLiteralDescription(targetField.getType());
        descriptor.setField(targetField);
        descriptor.setProperty(targetDescriptor);
        return descriptor;
    }

    public static LoadLiteral createLiteralDescription(Class<?> type)
            throws Loader.CanNotDeserializeObject {
        // Target properties.
        PropertyDescriptor valueDescriptor = null;
        PropertyDescriptor langDescriptor = null;

        // List of all field.
        final List<Field> fields = new ArrayList<>(12);
        Class<?> currentClass = type;
        do {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null);

        //
        for (Field field : fields) {
            // TODO Check field type, it should be string!
            // Get basic information about field.
            if (field.getAnnotation(RdfToPojo.Property.class) != null) {
                LOG.warn("Property detected inside the Value class: '"
                        + type.getName() + "'.");
                continue;
            }
            if (field.getAnnotation(RdfToPojo.Value.class) != null) {
                valueDescriptor = getPropertyDescriptor(field, type);
            } else if (field.getAnnotation(RdfToPojo.Lang.class) != null) {
                langDescriptor = getPropertyDescriptor(field, type);
            }
        }

        return new LoadLiteral(valueDescriptor, langDescriptor, null, null);
    }

    private static void append(Map<String, List<Loader>> data, String iri,
            Loader newLoader) {
        List<Loader> loaders = data.get(iri);
        if (loaders == null) {
            loaders = new LinkedList<>();
            data.put(iri, loaders);
        }
        loaders.add(newLoader);
    }

    public static boolean isPrimitive(Class<?> fieldClass) {
        return fieldClass.isPrimitive() || WRAP_TYPES.contains(fieldClass);
    }

    /**
     * Return generic type of a collection.
     *
     * @param type
     * @return
     */
    private static Class<?> getCollectionGenericType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            LOG.warn("Superclass it not ParameterizedType");
            return null;
        }
        final Type[] params =
                ((ParameterizedType) type).getActualTypeArguments();
        // We know there should be just one for Collection.
        if (params.length != 1) {
            LOG.warn("Unexpected number of generic types: {} (1 expected)",
                    params.length);
            return null;
        }
        if (!(params[0] instanceof Class)) {
            LOG.warn("Unexpected type '{}'", params[0].toString());
            return null;
        }
        return (Class<?>) params[0];
    }

    private static PropertyDescriptor getPropertyDescriptor(Field field,
            Class<?> ownerType) throws Loader.CanNotDeserializeObject {
        try {
            return new PropertyDescriptor(field.getName(), ownerType);
        } catch (IntrospectionException ex) {
            throw new Loader.CanNotDeserializeObject(
                    "Can't get property descriptor for: '" + field.getName()
                            + "' and class: '" + field.getType().getSimpleName()
                            + "' (missing getter/setter?).", ex);
        }
    }

}
