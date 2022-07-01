package com.linkedpipes.etl.executor.api.v1.rdf.pojo;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RdfToPojoLoader {

    private RdfToPojoLoader() {

    }

    public static void load(
            RdfSource source, String resource, Loadable entity)
            throws RdfException {
        Map<Loadable, String> newEntities = new HashMap<>();

        entity.resource(resource);
        source.statements(resource, (predicate, object) -> {
            Loadable newEntity = entity.load(predicate, object);
            if (newEntity != null) {
                newEntities.put(newEntity, object.asString());
            }
        });
        for (Map.Entry<Loadable, String> entry : newEntities.entrySet()) {
            load(source, entry.getValue(), entry.getKey());
        }
    }

    public static void loadByReflection(
            RdfSource source, Object entity) throws RdfException {
        Descriptor descriptor = new Descriptor(entity.getClass());
        String typeName = descriptor.getObjectType();
        List<String> resources = source.getByType(typeName);
        if (resources.size() != 1) {
            throw new RdfException("Invalid number of resources ({})",
                    resources.size());
        }
        loadByReflection(source, resources.get(0), entity);
    }

    public static void loadByReflection(
            RdfSource source, String resource, Object entity)
            throws RdfException {
        ReflectionLoader entityWrap = new ReflectionLoader(entity);
        load(source, resource, entityWrap);
    }

}
