package com.linkedpipes.etl.executor.api.v1.rdf.pojo;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.lang.reflect.Field;

final class Descriptor {

    final private Class<?> describedType;

    public Descriptor(Class<?> clazz) {
        this.describedType = clazz;
    }

    public String getObjectType() {
        RdfToPojo.Type annotation = describedType.getAnnotation(
                RdfToPojo.Type.class);
        if (annotation == null) {
            return null;
        } else {
            return annotation.iri();
        }
    }

    public Field getFieldForResource() {
        return getResourceField(describedType);
    }

    private Field getResourceField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(RdfToPojo.Resource.class) != null) {
                return field;
            }
        }
        if (clazz.getSuperclass() == null) {
            return null;
        } else {
            return getResourceField(clazz.getSuperclass());
        }
    }

    public Field getFieldForPredicate(String predicate) {
        return getPropertyField(predicate, describedType);
    }

    private Field getPropertyField(String predicate, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            RdfToPojo.Property prop = field.getAnnotation(
                    RdfToPojo.Property.class);
            if (prop != null && prop.iri().equals(predicate)) {
                return field;
            }
        }
        if (clazz.getSuperclass() == null) {
            return null;
        } else {
            return getPropertyField(predicate, clazz.getSuperclass());
        }
    }

}
