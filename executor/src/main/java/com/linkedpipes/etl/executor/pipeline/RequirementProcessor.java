package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Find requirements in pipeline definition and handle them.
 */
class RequirementProcessor {

    private RequirementProcessor() {
    }

    /**
     * Handle requirements on the pipeline definition.
     *
     * @param definition
     * @param graph
     * @param resourceManager
     */
    public static void handle(BackendRdfSource definition, String graph,
            ResourceManager resourceManager) throws LpException {
        final List<Map<String, String>> working;
        final List<Map<String, String>> input;
        try {
            working = RdfUtils.sparqlSelect(definition,
                    getWorkingDirectoryQuery(graph));
            input = RdfUtils.sparqlSelect(definition,
                    getInputDirectoryQuery(graph));
        } catch (RdfUtilsException ex) {
            throw new LpException("Can't query requirements.", ex);
        }
        //
        try {
            final RdfBuilder builder = RdfBuilder.create(definition, graph);
            for (Map<String, String> entry : working) {
                final String iri = entry.get("s");
                final File file = resourceManager.getWorkingDirectory("temp");
                builder.entity(iri).iri(LP_EXEC.HAS_WORKING_DIRECTORY,
                        file.toURI().toString());
            }
            final File inputDirectory = resourceManager.getInputDirectory();
            for (Map<String, String> entry : input) {
                final String iri = entry.get("s");
                builder.entity(iri).iri(LP_EXEC.HAS_INPUT_DIRECTORY,
                        inputDirectory.toURI().toString());
            }
            builder.commit();
        } catch (RdfUtilsException ex) {
            throw new LpException("Can't create save RDF.", ex);
        }
    }

    private static String getWorkingDirectoryQuery(String graph) {
        return "SELECT ?s WHERE { GRAPH <" + graph + "> {\n" +
                " ?s <" + LP_PIPELINE.HAS_REQUIREMENT + "> " +
                "<" + LP_PIPELINE.WORKING_DIRECTORY + "> \n" +
                "}}";
    }

    private static String getInputDirectoryQuery(String graph) {
        return "SELECT ?s WHERE { GRAPH <" + graph + "> {\n" +
                " ?s <" + LP_PIPELINE.HAS_REQUIREMENT + "> " +
                "<" + LP_PIPELINE.INPUT_DIRECTORY + "> \n" +
                "}}";
    }

}
