package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class RdfLoader {

    /**
     * Interface for loading RDF into POJO object without the reflection.
     */
    public interface Loadable<ValueType> {

        RdfLoader.Loadable load(String predicate, ValueType object)
                throws RdfUtilsException;

    }

    /**
     * Object descriptor used for reflection based loading.
     */
    public interface Descriptor {

        String getType();

        Field getField(String predicate);

    }

    /**
     * Factory for descriptor objects. Used for reflection based
     * loading.
     */
    @FunctionalInterface
    public interface DescriptorFactory {

        Descriptor create(Class<?> type);

    }

    /**
     * Can be used to load string with language.
     */
    public interface LangString {

        void setValue(String value, String language);

    }

    public static <ValueType> void load(RdfSource source,
            Loadable<ValueType> loadable, String resource, String graph,
            Class<ValueType> clazz) throws RdfUtilsException {
        final Map<Loadable<ValueType>, String> toLoad = new HashMap<>();
        final RdfSource.ValueToString<ValueType> converter =
                source.toStringConverter(clazz);
        source.triples(resource, graph, clazz, (s, p, object) -> {
            // This is to fix the bad generic inference in the lambda function.
            // The clazz should force the handler to be of type
            // ValueType and the the object should be of type
            // ValueType, but it's not, so this is a workaround.
            final ValueType o = clazz.cast(object);
            final Loadable<ValueType> newObject = loadable.load(p, o);
            if (newObject != null) {
                toLoad.put(newObject, converter.asString(o));
            }
        });
        for (Map.Entry<Loadable<ValueType>, String> entry : toLoad.entrySet()) {
            load(source, entry.getKey(), entry.getValue(), graph, clazz);
        }
    }

    public static <ValueType> void loadByReflection(RdfSource source,
            DescriptorFactory descriptorFactory,
            Object object, String resource, String graph)
            throws RdfUtilsException {
        final RdfSource.ValueConverter<ValueType> stringConverter =
                source.valueConverter();
        final FieldLoader<ValueType> fieldLoader = new FieldLoader<>(
                stringConverter);
        final ReflectionLoader<ValueType> loader = new ReflectionLoader(
                object, descriptorFactory.create(object.getClass()),
                fieldLoader);
        load(source, loader, resource, graph, source.getDefaultType());
        // Load entities created during loading of this object.
        for (Map.Entry<Object, ValueType> entry
                : loader.getObjectsToLoad().entrySet()) {
            loadByReflection(source, descriptorFactory, entry.getKey(),
                    stringConverter.asString(entry.getValue()), graph);
        }
    }

}
