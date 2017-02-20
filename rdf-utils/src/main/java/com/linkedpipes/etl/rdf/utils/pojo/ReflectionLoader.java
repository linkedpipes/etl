package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Use reflection to load values into the given object.
 *
 * This wrap is created for every object that should be loaded via
 * a reflection.
 */
class ReflectionLoader<ValueType> implements RdfLoader.Loadable<ValueType> {

    private final Object objectToLoadInto;

    private final RdfLoader.Descriptor descriptor;

    private final FieldLoader<ValueType> fieldLoader;

    /**
     * Map of object, that were created during loading. Those
     * object must be loaded after the main object is loaded.
     */
    private final Map<Object, ValueType> objectsToLoad = new HashMap<>();

    public ReflectionLoader(Object objectToLoadInto,
            RdfLoader.Descriptor descriptor,
            FieldLoader<ValueType> fieldLoader) {
        this.objectToLoadInto = objectToLoadInto;
        this.descriptor = descriptor;
        this.fieldLoader = fieldLoader;
    }

    @Override
    public RdfLoader.Loadable load(String predicate, ValueType object)
            throws RdfUtilsException {
        final Field field = descriptor.getField(predicate);
        if (field == null) {
            // Some predicates may not map to any field.
            return null;
        }
        // Load value into the object.
        final Object newObject;
        try {
            newObject = fieldLoader.set(objectToLoadInto, field, object, true);
        } catch (FieldLoader.CantLoadValue ex) {
            throw new RdfUtilsException("Can't load field value (predicate:{})",
                    predicate, ex);
        }
        if (newObject != null) {
            objectsToLoad.put(newObject, object);
        }
        return null;
    }

    /**
     * Return all inner objects of this object, they must be loaded before
     * the loading of this object is complete.
     * <br/>
     * They must be loaded only when loading of this object has been completed.
     *
     * @return
     */
    public Map<Object, ValueType> getObjectsToLoad() {
        return Collections.unmodifiableMap(objectsToLoad);
    }

}
