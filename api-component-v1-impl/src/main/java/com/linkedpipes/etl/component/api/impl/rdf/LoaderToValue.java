package com.linkedpipes.etl.component.api.impl.rdf;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * Specialization of loader used to load a simple value.
 */
abstract class LoaderToValue extends Loader {

    /**
     * Used to access getter and setter of field.
     */
    protected PropertyDescriptor property;

    /**
     * Field this descriptor describes.
     */
    protected Field field;

    protected LoaderToValue(PropertyDescriptor property, Field field) {
        this.property = property;
        this.field = field;
    }

    /**
     * Set value to the given object.
     *
     * @param object
     * @param value
     * @param valueAsString
     */
    protected void set(Object object, Object value, String valueAsString)
            throws CanNotDeserializeObject {
        try {
            property.getWriteMethod().invoke(object, value);
        } catch (Exception ex) {
            throw new CanNotDeserializeObject(
                    "Can't set property '" + field.getName() + "'"
                            + " (RDF deserialization) to value: "
                            + valueAsString, ex);
        }
    }

}
