package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.component.api.service.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Loader used to load to collection field type.
 */
class LoadCollection extends LoaderToValue {

    private static final Logger LOG
            = LoggerFactory.getLogger(LoadCollection.class);

    private final List<Class<?>> types;

    private final RdfReader.MergeOptionsFactory optionsFactory;

    LoadCollection(List<Class<?>> types, PropertyDescriptor property,
            Field field, RdfReader.MergeOptionsFactory optionsFactory) {
        super(property, field);
        this.types = types;
        this.optionsFactory = optionsFactory;
    }

    @Override
    public void load(Object object, Map<String, String> property, String graph,
            SparqlSelect select) throws CanNotDeserializeObject {
        // Get collection.
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
                            + "' on class: '" +
                            object.getClass().getCanonicalName()
                            + "'");
        }
        // Load object - here we need to decide based on the
        // object type.
        for (Class<?> type : types) {
            Object value = null;
            try {
                if (type.getAnnotation(RdfToPojo.Type.class) != null) {
                    value = LoadObject.loadNew(type, property.get("value"),
                            graph, select, optionsFactory);
                } else if (type.getAnnotation(RdfToPojo.Value.class) != null) {
                    value = LoadLiteral.loadNew(type, property);
                } else if (DescriptionFactory.isPrimitive(type)) {
                    // The type can be a primitive one.
                    value = LoadPrimitive.loadPrimitive(type,
                            property.get("value"));
                } else {
                    LOG.warn("Unknown type.");
                }
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
