package com.linkedpipes.etl.executor.api.v1.plugin;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.context.Context;

/**
 *
 * @author Škoda Petr
 */
public interface ExecutionListener {

    public static class InitializationFailure extends Exception {

        public InitializationFailure(String message) {
            super(message);
        }

        public InitializationFailure(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Is called whenever new execution is about to be executed. All the given objects are valid to the end of pipeline
     * execution.
     *
     * @param definition
     * @param resoureUri
     * @param graph
     * @param context
     * @throws InitializationFailure
     */
    public void onExecutionBegin(SparqlSelect definition, String resoureUri, String graph, Context context)
            throws InitializationFailure;

    /**
     * Is called after the execution, after this point no resources created during pipeline execution are utilized any
     * further and so they should be released.
     *
     * By this call the context passed in onExecutionBegin is invalidated.
     */
    public void onExecutionEnd();

}
