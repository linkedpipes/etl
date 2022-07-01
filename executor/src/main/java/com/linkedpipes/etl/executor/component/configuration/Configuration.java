package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.executor.pipeline.model.ConfigurationDescription;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.executor.rdf.entity.EntityMerger;
import com.linkedpipes.etl.executor.rdf.entity.EntityReference;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage configurations for components.
 *
 * <p>For each component the effective configuration is computed
 * before the component execution.
 */
public class Configuration {

    public enum Status {
        FORCE,
        FORCED,
        INHERIT,
        INHERIT_AND_FORCE,
        MERGE,
        NONE
    }

    /**
     * Prepare configuration for given component and put it into given
     * definitionGraph.
     *
     * <p>If runtimeSource or runtimeGraph is null no runtime configuration is
     * used. Given runtimeSource is not shut down in this method.
     *
     * @param iri           Output configuration iri.
     * @param component     Component for which prepare the configuration.
     * @param runtimeSource Source for runtime configuration, can be null.
     * @param runtimeGraph  Graph with runtime configuration, can be null.
     * @param writer        Writer for the final configuration.
     * @param pipeline      Pipeline with definitions.
     */
    public static void prepareConfiguration(
            String iri, PipelineComponent component,
            BackendRdfSource runtimeSource, String runtimeGraph,
            BackendTripleWriter writer, Pipeline pipeline)
            throws ExecutorException {
        List<EntityReference> references = new ArrayList<>();
        ConfigurationDescription description =
                component.getConfigurationDescription();
        Rdf4jSource pipelineSource = pipeline.getSource();
        String configurationType = description.getDescribedType();

        // Get reference for configuration in the pipeline.
        EntityReference componentConfiguration = loadConfigurationReference(
                pipelineSource,
                component.getConfigurationGraph(),
                configurationType);
        try {
            references.add(SubstituteEnvironment.substitute(
                    System.getenv(),
                    pipelineSource, componentConfiguration, configurationType));
        } catch (RdfUtilsException ex) {
            throw new ExecutorException("Can't update configuration.", ex);
        }

        // Get reference for configuration in
        if (runtimeSource != null && runtimeGraph != null) {
            references.add(loadConfigurationReference(
                    runtimeSource,
                    runtimeGraph,
                    configurationType));
        }

        // Merge.
        try {
            (new EntityMerger(new DefaultControlFactory(pipelineSource)))
                    .merge(references, iri, writer);
        } catch (RdfUtilsException ex) {
            throw new ExecutorException("Can't merge data.", ex);
        }
    }

    private static EntityReference loadConfigurationReference(
            BackendRdfSource source, String graph, String configurationType)
            throws ExecutorException {
        String query = queryForTypes(
                configurationType, graph);
        String resource;
        try {
            resource = RdfUtils.sparqlSelectSingle(source, query, "resource");
        } catch (RdfUtilsException ex) {
            throw new ExecutorException(
                    "Can't get configuration object of type {} in {}",
                    configurationType, graph, ex);
        }
        return new EntityReference(resource, graph, source);
    }

    private static String queryForTypes(String type, String graph) {
        return "SELECT ?resource WHERE { GRAPH <" + graph + "> {\n"
                + "   ?resource a <" + type + ">\n"
                + "} }";
    }


}
