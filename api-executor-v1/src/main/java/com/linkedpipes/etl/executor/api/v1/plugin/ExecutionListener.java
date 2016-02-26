package com.linkedpipes.etl.executor.api.v1.plugin;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.context.Context;
import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException;
import java.util.Arrays;

/**
 * Specialized listener designed to enable components monitor a pipeline execution.
 *
 * @author Škoda Petr
 */
public interface ExecutionListener {

    public static class InitializationFailure extends LocalizedException {

        public InitializationFailure(String messages, Object... args) {
            super(Arrays.asList(new LocalizedString(messages, "en")), args);
        }

    }

    /**
     * Is called whenever new execution is about to be executed. All the given objects are valid to the end of pipeline
     * execution.
     *
     * @param definition Access to the SPARQL-like interface of the pipeline definition.
     * @param resoureUri Pipeline resource URI.
     * @param graph Name of graph with definition.
     * @param context Application context.
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
