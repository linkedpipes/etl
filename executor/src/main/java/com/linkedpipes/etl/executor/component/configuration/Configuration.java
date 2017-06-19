package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.model.Component;
import com.linkedpipes.etl.executor.pipeline.model.ConfigurationDescription;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.entity.EntityMerger;
import com.linkedpipes.etl.rdf.utils.entity.EntityReference;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage configurations for components.
 *
 * For each component the effective configuration is computed
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
     * If runtimeSource or runtimeGraph is null no runtime configuration is
     * used. Given runtimeSource is not shut down in this method.
     *
     * @param iri Output configuration iri.
     * @param component Component for which prepare the configuration.
     * @param runtimeSource Source for runtime configuration, can be null.
     * @param runtimeGraph Graph with runtime configuration, can be null.
     * @param writer Writer for the final configuration.
     * @param pipeline Pipeline with definitions.
     */
    public static void prepareConfiguration(String iri,
            Component component, RdfSource runtimeSource,
            String runtimeGraph, TripleWriter writer,
            Pipeline pipeline)
            throws ExecutorException {
        final List<EntityReference> references = new ArrayList<>(3);
        final ConfigurationDescription description =
                component.getConfigurationDescription();
        final RdfSource pplSource = pipeline.getSource();
        final String configurationType = description.getDescribedType();
        // Get definitionGraph and resource for each configuration.
        for (com.linkedpipes.etl.executor.pipeline.model.Configuration configuration
                : component.getConfigurations()) {
            final String graph = configuration.getGraph();
            final String query = getQueryForConfiguration(
                    configurationType, graph);
            final String resource;
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
            final String query = getQueryForConfiguration(
                    configurationType, runtimeGraph);
            final String resource;
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
        final DefaultControlFactory controlFactory =
                new DefaultControlFactory(pplSource,
                        pipeline.getPipelineGraph());
        final EntityMerger merger = new EntityMerger(controlFactory);
        try {
            merger.merge(references, iri, writer);
        } catch (RdfUtilsException ex) {
            throw new ExecutorException("Can't merge data.", ex);
        }
    }

    private static String getQueryForConfiguration(String type, String graph) {
        return "SELECT ?resource WHERE { GRAPH <" + graph + "> {\n" +
                "   ?resource a <" + type + ">\n" +
                "} }";
    }

}
