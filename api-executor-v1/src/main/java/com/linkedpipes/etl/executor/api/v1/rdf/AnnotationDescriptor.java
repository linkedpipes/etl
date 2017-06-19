package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.rdf.utils.pojo.Descriptor;

import java.lang.reflect.Field;

class AnnotationDescriptor implements Descriptor {

    final private Class<?> describedType;

    public AnnotationDescriptor(Class<?> clazz) {
        this.describedType = clazz;
    }

    @Override
    public String getObjectType() {
        RdfToPojo.Type annotation = describedType.getAnnotation(
                RdfToPojo.Type.class);
        if (annotation == null) {
            return null;
        } else {
            return annotation.iri();
        }
    }

    @Override
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

    @Override
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
