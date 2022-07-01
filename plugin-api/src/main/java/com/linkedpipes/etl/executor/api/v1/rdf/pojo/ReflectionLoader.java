package com.linkedpipes.etl.executor.api.v1.rdf.pojo;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;

import java.lang.reflect.Field;

class ReflectionLoader implements Loadable {

    private final Object targetObject;

    private Descriptor descriptor;

    private final FieldLoader fieldLoader = new FieldLoader();

    public ReflectionLoader(Object object) {
        this.targetObject = object;
        this.descriptor = new Descriptor(object.getClass());
    }


    @Override
    public void resource(String resource) throws RdfException {
        Field field = descriptor.getFieldForResource();
        if (field == null) {
            return;
        }
        FieldUtils.setValue(targetObject, field, resource);
    }

    @Override
    public Loadable load(String predicate, RdfValue value)
            throws RdfException {
        Field field = descriptor.getFieldForPredicate(predicate);
        if (field == null) {
            return null;
        }
        Object newObject = fieldLoader.set(targetObject, field, value, true);
        if (newObject == null) {
            return null;
        } else {
            ReflectionLoader loader = new ReflectionLoader(newObject);
            return loader;
        }
    }

}
