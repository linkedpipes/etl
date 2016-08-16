package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Petr Škoda
 */
class LoadLiteral extends LoaderToValue {

    private final PropertyDescriptor valueDescriptor;

    private final PropertyDescriptor languageDescriptor;

    LoadLiteral(PropertyDescriptor valueDescriptor,
            PropertyDescriptor languageDescriptor) {
        super(null, null);
        this.valueDescriptor = valueDescriptor;
        this.languageDescriptor = languageDescriptor;
    }

    LoadLiteral(PropertyDescriptor valueDescriptor,
            PropertyDescriptor languageDescriptor,
            PropertyDescriptor property, Field field) {
        super(property, field);
        this.valueDescriptor = valueDescriptor;
        this.languageDescriptor = languageDescriptor;
    }

    @Override
    public void load(Object object, Map<String, String> property, String graph,
            SparqlSelect select) throws CanNotDeserializeObject {
        // Create new instance.
        final Object value;
        try {
            value = field.getType().newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new CanNotDeserializeObject("Can't create object instance.",
                    ex);
        }
        loadToObject(value, property);
        //
        set(object, value, property.get("value"));
    }

    protected void loadToObject(Object value, Map<String, String> property)
            throws CanNotDeserializeObject {
        // Store values.
        if (valueDescriptor != null) {
            try {
                valueDescriptor.getWriteMethod().invoke(value,
                        property.get("value"));
            } catch (Exception ex) {
                throw new CanNotDeserializeObject("Can't set value in object: '"
                        + field.getType().getName() + "'.", ex);
            }
        }
        if (languageDescriptor != null) {
            try {
                languageDescriptor.getWriteMethod().invoke(value,
                        property.get("language"));
            } catch (Exception ex) {
                throw new CanNotDeserializeObject("Can't set value in object: '"
                        + field.getType().getName() + "'.", ex);
            }
        }
    }

    /**
     * Load and return new literal object.
     *
     * @param type
     * @param property
     * @return
     */
    static Object loadNew(Class<?> type, Map<String, String> property)
            throws CanNotDeserializeObject {
        final LoadLiteral loader
                = DescriptionFactory.createLiteralDescription(type);
        // Create new instance.
        final Object value;
        try {
            value = type.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new CanNotDeserializeObject("Can't create object instance.",
                    ex);
        }
        loader.loadToObject(value, property);
        return value;
    }

    public void setProperty(PropertyDescriptor property) {
        this.property = property;
    }

    public void setField(Field field) {
        this.field = field;
    }

}
