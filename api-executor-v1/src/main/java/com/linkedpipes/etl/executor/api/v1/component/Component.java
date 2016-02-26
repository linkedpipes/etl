package com.linkedpipes.etl.executor.api.v1.component;

import java.util.Map;

import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.context.ExecutionContext;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.util.Arrays;

/**
 * Base interface for an executable component.
 *
 * @author Škoda Petr
 */
public interface Component {

    public class InitializationFailed extends NonRecoverableException {

        public InitializationFailed(String messages, Object... args) {
            super(Arrays.asList(new LocalizedString(messages, "en")), args);
        }

    }

    public static class ComponentFailed extends NonRecoverableException {

        public ComponentFailed(String messages, Object... args) {
            super(Arrays.asList(new LocalizedString(messages, "en")), args);
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
