package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;

import java.lang.reflect.Field;

/**
 * Annotations based description factory for {@link RdfLoader}.
 */
public class AnnotationDescriptionFactory
        implements RdfLoader.DescriptorFactory {

    protected static class Descriptor implements RdfLoader.Descriptor {

        final private Class<?> clazz;

        public Descriptor(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public String getType() {
            final RdfToPojo.Type annotation =
                    clazz.getAnnotation(RdfToPojo.Type.class);
            if (annotation == null) {
                return null;
            } else {
                return annotation.iri();
            }
        }

        @Override
        public Field getField(String predicate) {
            return getField(predicate, clazz);
        }

        private Field getField(String predicate, Class<?> clazz) {
            for (Field field : clazz.getDeclaredFields()) {
                final RdfToPojo.Property prop =
                        field.getAnnotation(RdfToPojo.Property.class);
                if (prop != null && prop.iri().equals(predicate)) {
                    return field;
                }
            }
            // Check super class.
            if (clazz.getSuperclass() == null) {
                return null;
            } else {
                return getField(predicate, clazz.getSuperclass());
            }
        }

    }

    public AnnotationDescriptionFactory() {
    }

    @Override
    public RdfLoader.Descriptor create(Class<?> type) {
        return new Descriptor(type);
    }

}
