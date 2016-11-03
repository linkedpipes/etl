package com.linkedpipes.etl.component.api;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Basic component definition.
 */
public interface Component {

    /**
     * Interface of component designed for sequential execution.
     */
    public interface Sequential {

        /**
         * Perform execution of the component.
         */
        void execute() throws LpException;

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Inject {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Configuration {

    }

    /**
     * Mark data unit as an component input.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface InputPort {

        String id();

        boolean optional() default false;

    }

    /**
     * Mark data unit as an component output.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface OutputPort {

        String id();

    }

    /**
     * Mark data unit as a source for the runtime configuration. Data from
     * the data unit are used to load configuration and have highest
     * priority.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ContainsConfiguration {

    }

}
