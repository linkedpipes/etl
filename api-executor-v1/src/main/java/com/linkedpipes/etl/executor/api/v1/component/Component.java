package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.event.Event;

import java.util.Map;

/**
 * Base interface for a component.
 */
public interface Component {

    /**
     * Represent a list of headers that component should implement.
     */
    public final class Headers {

        private Headers() {
        }

        /**
         * Comma separated list of packages, that are considered to be part
         * of a component. Only logs from these packages are stored
         * in a component log file.
         */
        public static final String LOG_PACKAGES = "log.packages";

    }

    /**
     * Context given to components.
     */
    public interface Context {

        public void sendMessage(Event message);

    }

    /**
     * Initialize component before execution.
     *
     * @param dataUnits
     */
    public void initialize(Map<String, DataUnit> dataUnits) throws RdfException;

    /**
     * Used to get additional information about the component.
     *
     * @param key
     * @return Stored value or null.
     */
    public String getHeader(String key);

}
