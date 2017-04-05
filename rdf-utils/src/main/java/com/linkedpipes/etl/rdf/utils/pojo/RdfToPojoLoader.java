package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;

import java.util.HashMap;
import java.util.Map;

public class RdfToPojoLoader {

    private RdfToPojoLoader() {

    }

    public static void loadResource(RdfSource source,
            String resource, String graph, Loadable entity)
            throws RdfUtilsException {
        Map<Loadable, String> newEntities = new HashMap<>();

        entity.resource(resource);
        source.triples(resource, graph, triple -> {
            Loadable newEntity = entity.load(
                    triple.getPredicate(), triple.getObject());
            if (newEntity != null) {
                newEntities.put(newEntity, triple.getObject().asString());
            }
        });

        for (Map.Entry<Loadable, String> entry : newEntities.entrySet()) {
            loadResource(source, entry.getValue(), graph, entry.getKey());
        }
    }

    public static void loadResourceByReflection(RdfSource source,
            String resource, String graph, Object entity,
            DescriptorFactory descriptorFactory)
            throws RdfUtilsException {
        ReflectionLoader entityWrap = new ReflectionLoader(
                descriptorFactory, entity);
        entityWrap.initialize();
        loadResource(source, resource, graph, entityWrap);
    }

}
