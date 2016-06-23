package com.linkedpipes.etl.component.api.impl.rdf;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 *
 * @author Petr Škoda
 */
abstract class LoaderToValue extends Loader {

    /**
     * Used to access getter and setter of field.
     */
    protected final PropertyDescriptor property;

    /**
     * Field this descriptor describes.
     */
    protected final Field field;

    protected LoaderToValue(PropertyDescriptor property, Field field) {
        this.property = property;
        this.field = field;
    }

    /**
     * Set value.
     *
     * @param object
     * @param value
     * @param valueAsString
     * @throws CanNotDeserializeObject
     */
    protected void set(Object object, Object value, String valueAsString)
            throws CanNotDeserializeObject {
        try {
            property.getWriteMethod().invoke(object, value);
        } catch (Exception ex) {
            throw new CanNotDeserializeObject(
                    "Can't set propety '" + field.getName() + "'"
                    + " (RDF deserialization) to value: "
                    + valueAsString, ex);
        }
    }

}
