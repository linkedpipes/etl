package com.linkedpipes.etl.rdf.utils.pojo;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class FieldUtils {

    private FieldUtils() {

    }

    public static Object getValue(Object object, Field field)
            throws LoaderException {
        if (Modifier.isPublic(field.getModifiers())) {
            return getByField(object, field);
        } else {
            return getByGetter(object, field);
        }
    }

    private static Object getByField(Object object, Field field)
            throws LoaderException {
        try {
            return field.get(object);
        } catch (IllegalAccessException ex) {
            throw new LoaderException("Can't get value (by field): {}",
                    field.getName(), ex);
        }
    }

    private static Object getByGetter(Object object, Field field)
            throws LoaderException {
        PropertyDescriptor descriptor = getDescriptor(field);
        try {
            return descriptor.getReadMethod().invoke(object);
        } catch (Throwable ex) {
            throw new LoaderException("Can't get value (by getter): {}",
                    field.getName(), ex);
        }
    }

    private static PropertyDescriptor getDescriptor(Field field)
            throws LoaderException {
        try {
            return new PropertyDescriptor(field.getName(),
                    field.getDeclaringClass());
        } catch (IntrospectionException ex) {
            throw new LoaderException("Can't handle property descriptor.");
        }
    }

    public static void setValue(Object object, Field field, Object value)
            throws LoaderException {
        if (Modifier.isPublic(field.getModifiers())) {
            setByField(object, field, value);
        } else {
            setBySetter(object, field, value);
        }
    }

    private static void setByField(Object object, Field field, Object value)
            throws LoaderException {
        try {
            field.set(object, value);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new LoaderException("Can't set value (by field): {}",
                    field.getName(), ex);
        }
    }

    private static Object setBySetter(Object object, Field field, Object value)
            throws LoaderException {
        PropertyDescriptor descriptor = getDescriptor(field);
        try {
            return descriptor.getWriteMethod().invoke(object, value);
        } catch (Throwable ex) {
            throw new LoaderException("Can't set value (by getter): {}",
                    field.getName(), ex);
        }
    }

}
