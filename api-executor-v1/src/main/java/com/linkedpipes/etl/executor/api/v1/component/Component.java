package com.linkedpipes.etl.executor.api.v1.component;

import java.util.Map;

import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.context.ExecutionContext;

/**
 * Base interface for an executable component.
 *
 * @author Škoda Petr
 */
public interface Component {

    public class InitializationFailed extends Exception {

        public InitializationFailed(String message) {
            super(message);
        }

        public InitializationFailed(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public static class ComponentFailed extends Exception {

        public ComponentFailed(String message) {
            super(message);
        }

        public ComponentFailed(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Prepare to use.
     *
     * @param dataUnits
     * @param context
     * @throws com.linkedpipes.etl.executor.api.v1.component.Component.InitializationFailed
     */
    public void initialize(Map<String, DataUnit> dataUnits, ExecutionContext context) throws InitializationFailed;

    /**
     * Execute task with given content.
     *
     * @param context
     * @throws com.linkedpipes.etl.executor.api.v1.component.Component.ComponentFailed
     */
    public void execute(ExecutionContext context) throws ComponentFailed;

    /**
     *
     * @param key
     * @return Value stored under given header or null if header of given key is not presented.
     */
    public String getHeader(String key);

}
