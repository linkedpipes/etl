package com.linkedpipes.etl.executor.api.v1;

import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.util.Arrays;

/**
 *
 * @author Petr Škoda
 */
public interface Plugin {

    public interface Context {

        public void sendMessage(Event message);

    }

    public interface MessageListener {

        /**
         * Called whenever message is published in the system.
         *
         * @param message
         */
        public void onMessage(Event message);

    }

    public interface ExecutionListener {

        public static class InitializationFailure extends LocalizedException {

            public InitializationFailure(String messages, Object... args) {
                super(Arrays.asList(new LocalizedException.LocalizedString(
                        messages, "en")), args);
            }

        }

        /**
         * Is called whenever new execution is about to be executed. All the
         * given objects are valid to the end of pipeline execution.
         *
         * @param definition SPARQL-like interface of the pipeline definition.
         * @param resourceIri Pipeline resource IRI.
         * @param graph Name of graph with definition.
         * @param context Application context.
         * @throws InitializationFailure
         */
        public void onExecutionBegin(SparqlSelect definition, String resourceIri,
                String graph, Context context) throws InitializationFailure;

        /**
         * Is called after the execution, after this point no resources created
         * during pipeline execution are utilized any further and so they should
         * be released.
         *
         * By this call the context passed in onExecutionBegin is invalidated.
         */
        public void onExecutionEnd();

    }

}
