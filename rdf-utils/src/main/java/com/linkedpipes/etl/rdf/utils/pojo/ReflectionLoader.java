package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.model.RdfValue;

import java.lang.reflect.Field;

class ReflectionLoader implements Loadable {

    private final DescriptorFactory descriptorFactory;

    private final Object targetObject;

    private Descriptor descriptor;

    private final FieldLoader fieldLoader = new FieldLoader();

    public ReflectionLoader(DescriptorFactory descriptorFactory,
            Object object) {
        this.descriptorFactory = descriptorFactory;
        this.targetObject = object;
    }

    public void initialize() throws LoaderException {
        this.descriptor = descriptorFactory.create(targetObject.getClass());
        if (this.descriptor == null) {
            throw new LoaderException("Missing description for: {}",
                    targetObject.getClass());
        }
    }

    @Override
    public void resource(String resource) throws LoaderException {
        Field field = descriptor.getFieldForResource();
        if (field == null) {
            return;
        }
        FieldUtils.setValue(targetObject, field, resource);
    }

    @Override
    public Loadable load(String predicate, RdfValue value)
            throws LoaderException {
        Field field = descriptor.getFieldForPredicate(predicate);
        if (field == null) {
            return null;
        }
        Object newObject = fieldLoader.set(targetObject, field, value, true);
        if (newObject == null) {
            return null;
        } else {
            ReflectionLoader loader = new ReflectionLoader(
                    descriptorFactory, newObject);
            loader.initialize();
            return loader;
        }
    }
}
