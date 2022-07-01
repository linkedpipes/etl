package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.event.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base interface for a component. The component is a base composition unit
 * of a pipeline.
 */
public interface Component {

    /**
     * Component execution context.
     */
    interface Context {

        void sendMessage(Event message);

        /**
         * Return true if component execution was cancelled.
         */
        boolean isCancelled();

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Inject {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Configuration {

    }

    /**
     * Mark data unit as an component input.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface InputPort {

        String iri();

    }

    /**
     * Mark data unit as an component output.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface OutputPort {

        String iri();

    }

    /**
     * Mark data unit as a source for the runtime configuration. Data from
     * the data unit are used to load configuration and have highest
     * priority.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ContainsConfiguration {

    }

}
