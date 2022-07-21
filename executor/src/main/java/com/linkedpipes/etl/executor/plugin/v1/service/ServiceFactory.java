package com.linkedpipes.etl.executor.plugin.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.service.DefinitionReader;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.service.WorkingDirectory;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP;

import java.net.URI;
import java.util.List;

public class ServiceFactory {

    protected ServiceFactory() {
    }

    public static Object create(
            Class<?> serviceType, String component,
            RdfSource definition, Component.Context context)
            throws LpException {
        if (serviceType.equals(ProgressReport.class)) {
            return new DefaultProgressReport(context, component);
        }
        if (serviceType.equals(DefinitionReader.class)) {
            return new DefaultDefinitionReader(component, definition);
        }
        if (serviceType.equals(WorkingDirectory.class)) {
            return createWorkingDirectory(component, definition);
        }
        throw new LpException("Invalid service type: {}",
                serviceType.getName());
    }

    private static WorkingDirectory createWorkingDirectory(
            String component, RdfSource definition)
            throws LpException {
        List<RdfValue> paths = definition.getPropertyValues(
                component, LP.HAS_WORKING_DIRECTORY);
        if (paths.size() != 1) {
            throw new LpException("Invalid number of working paths: {}",
                    paths.size());
        }
        return new WorkingDirectory(URI.create(paths.get(0).asString()));
    }

}
