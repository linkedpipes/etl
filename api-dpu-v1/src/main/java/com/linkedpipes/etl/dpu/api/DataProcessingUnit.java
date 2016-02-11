package com.linkedpipes.etl.dpu.api;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Petr Škoda
 */
public interface DataProcessingUnit {

    public interface Context {

        /**
         * Return true if the execution of current {@link DPU} should be stopped as soon as possible.
         *
         * @return True if the execution should stop.
         */
        public boolean canceled();

        public String getComponentUri();

        public File getWorkingDirectory();

    }

    public class ExecutionFailed extends NonRecoverableException {

        final Object[] args;

        public ExecutionFailed(String message, Object... args) {
            super(message);
            this.args = args;
        }

        public ExecutionFailed(Throwable cause, String message, Object... args) {
            super(message, cause);
            this.args = args;
        }

        public Object[] getArgs() {
            return args;
        }

    }

    public class ExecutionCancelled extends ExecutionFailed {

        public ExecutionCancelled() {
            super("execution.cancel");
        }

    }

    /**
     * Used to assign type to the component. Use types from {@link com.linkedpipes.dpu.basic.vocabulary.BaseTerms}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Type {

        String[] value();

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Extension {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Configuration {

        Class<?>[] history() default {};

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface InputPort {

        String id();

        boolean optional() default false;

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface OutputPort {

        String id();

    }

}
