package com.linkedpipes.etl.executor.api.v1;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

/**
 * Interface used by plugins. Plugins are basic units that can be loaded
 * into the application.
 *
 * @author Petr Škoda
 */
public interface Plugin {

    public interface PipelineListener {

        /**
         * Is called whenever new execution is about to be executed. All the
         * given objects are valid to the end of pipeline execution.
         *
         * @param definition SPARQL-like interface of the pipeline definition.
         * @param resourceIri Pipeline resource IRI.
         * @param graph Name of graph with definition.
         */
        public void onPipelineBegin(SparqlSelect definition,
                String resourceIri,
                String graph) throws RdfException;

        /**
         * Is called after the execution, after this point no resources created
         * during pipeline execution are utilized any further and so they should
         * be released.
         * <p>
         * By this call the context passed in onPipelineBegin is invalidated.
         */
        public void onPipelineEnd();

    }

}


