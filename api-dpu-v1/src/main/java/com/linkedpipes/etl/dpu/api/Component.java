package com.linkedpipes.etl.dpu.api;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

/**
 * This class provide basic definition that should by new components.
 *
 * @author Petr Škoda
 */
public interface Component {

    public interface Context {

        /**
         * Return true if the execution of current {@link DPU}
         * should be stopped as soon as possible.
         *
         * @return True if the execution should stop.
         */
        public boolean canceled();

        public String getComponentIri();

        public File getWorkingDirectory();

    }

    /**
     * Base class for exception and failure reporting.
     *
     * The reference of arguments in message must by done by '{}' string.
     */
    public class ExecutionFailed extends NonRecoverableException {

        public ExecutionFailed(String message, Object... args) {
            super(Arrays.asList(new LocalizedString(message, "en")), args);
        }

    }

    /**
     * Use to report execution cancellation.
     */
    public class ExecutionCancelled extends ExecutionFailed {

        public ExecutionCancelled() {
            super("Execution cancelled.");
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Inject {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Configuration {

        Class<?>[] history() default {};

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
