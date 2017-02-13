package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

import java.net.URI;

public class DefaultServiceFactory implements ServiceFactory {

    @Override
    public Object create(Class<?> serviceType, String component, String graph,
            RdfSource definition, Component.Context context)
            throws LpException {
        if (serviceType.equals(ExceptionFactory.class)) {
            return new DefaultExceptionFactory(component);
        }
        if (serviceType.equals(ProgressReport.class)) {
            return new DefaultProgressReport(context, component);
        }
        if (serviceType.equals(DefinitionReader.class)) {
            return new DefaultDefinitionReader(component, graph, definition);
        }
        if (serviceType.equals(WorkingDirectory.class)) {
            try {
                return createWorkingDirectory(component, graph, definition);
            } catch (RdfUtilsException ex) {
                throw new LpException("Can't get working directory for: {}",
                        component, ex);
            }
        }
        return null;
    }

    private WorkingDirectory createWorkingDirectory(
            String component, String graph, RdfSource definition) throws
            RdfUtilsException {
        final String path = RdfUtils.sparqlSelectSingle(definition, "" +
                "SELECT ?path WHERE { " +
                " GRAPH <" + graph + "> { " +
                "  <" + component + "> <" + LP_EXEC.HAS_WORKING_DIRECTORY +
                "> ?path . " +
                "} }", "path");
        return new WorkingDirectory(URI.create(path));
    }

}
