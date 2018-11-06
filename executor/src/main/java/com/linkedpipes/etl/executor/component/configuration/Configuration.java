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

import java.util.ArrayList;
import java.util.List;

/**
 * Manage configurations for components.
 *
 * <p>For each component the effective configuration is computed
 * before the component execution.
 */
public class Configuration {

    enum Status {
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
            BackendRdfSource runtimeSource,
            String runtimeGraph, BackendTripleWriter writer,
            Pipeline pipeline)
            throws ExecutorException {
        List<EntityReference> references = new ArrayList<>();
        ConfigurationDescription description =
                component.getConfigurationDescription();
        BackendRdfSource pplSource = pipeline.getSource();
        String configurationType = description.getDescribedType();
        // Get definitionGraph and resource for each configuration.
        {
            String graph = component.getConfigurationGraph();
            String query = getQueryForConfiguration(
                    configurationType, graph);
            String resource;
            try {
                resource = RdfUtils.sparqlSelectSingle(pplSource,
                        query, "resource");
            } catch (RdfUtilsException ex) {
                throw new ExecutorException(
                        "Can't get configuration object of type {} in {}",
                        configurationType, graph, ex);
            }
            // Create a reference to the configuration.
            references.add(new EntityReference(resource, graph, pplSource));
        }
        // Get definitionGraph and resource for runtime configuration if
        // provided.
        if (runtimeSource != null && runtimeGraph != null) {
            String query = getQueryForConfiguration(
                    configurationType, runtimeGraph);
            String resource;
            try {
                resource = RdfUtils.sparqlSelectSingle(runtimeSource,
                        query, "resource");
                references.add(new EntityReference(resource, runtimeGraph,
                        runtimeSource));
            } catch (RdfUtilsException ex) {
                // The runtime configuration may not be provided.
            }
        }
        // Merge.
        DefaultControlFactory controlFactory = new DefaultControlFactory(
                pplSource, pipeline.getPipelineGraph());
        EntityMerger merger = new EntityMerger(controlFactory);
        try {
            merger.merge(references, iri, writer);
        } catch (RdfUtilsException ex) {
            throw new ExecutorException("Can't merge data.", ex);
        }
    }

    private static String getQueryForConfiguration(String type, String graph) {
        return "SELECT ?resource WHERE { GRAPH <" + graph + "> {\n"
                + "   ?resource a <" + type + ">\n"
                + "} }";
    }

}
