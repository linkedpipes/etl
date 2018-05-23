package com.linkedpipes.etl.executor.api.v1.rdf.pojo;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class FieldUtils {

    private FieldUtils() {

    }

    public static Object getValue(Object object, Field field)
            throws RdfException {
        if (Modifier.isPublic(field.getModifiers())) {
            return getByField(object, field);
        } else {
            return getByGetter(object, field);
        }
    }

    private static Object getByField(Object object, Field field)
            throws RdfException {
        try {
            return field.get(object);
        } catch (IllegalAccessException ex) {
            throw new RdfException(
                    "Can't get value (by field): {}",
                    field.getName(), ex);
        }
    }

    private static Object getByGetter(Object object, Field field)
            throws RdfException {
        PropertyDescriptor descriptor = getDescriptor(field);
        try {
            return descriptor.getReadMethod().invoke(object);
        } catch (Throwable ex) {
            throw new RdfException(
                    "Can't get value (by getter): {}",
                    field.getName(), ex);
        }
    }

    private static PropertyDescriptor getDescriptor(Field field)
            throws RdfException {
        try {
            return new PropertyDescriptor(field.getName(),
                    field.getDeclaringClass());
        } catch (IntrospectionException ex) {
            throw new RdfException(
                    "Can't create property descriptor for '{}' on class '{}'",
                    field.getName(), field.getDeclaringClass().getName(), ex);
        }
    }

    public static void setValue(Object object, Field field, Object value)
            throws RdfException {
        if (Modifier.isPublic(field.getModifiers())) {
            setByField(object, field, value);
        } else {
            setBySetter(object, field, value);
        }
    }

    private static void setByField(Object object, Field field, Object value)
            throws RdfException {
        try {
            field.set(object, value);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RdfException(
                    "Can't set value (by field): {}",
                    field.getName(), ex);
        }
    }

    private static Object setBySetter(Object object, Field field, Object value)
            throws RdfException {
        PropertyDescriptor descriptor = getDescriptor(field);
        try {
            return descriptor.getWriteMethod().invoke(object, value);
        } catch (Throwable ex) {
            throw new RdfException(
                    "Can't set value (by getter): {}",
                    field.getName(), ex);
        }
    }

}
