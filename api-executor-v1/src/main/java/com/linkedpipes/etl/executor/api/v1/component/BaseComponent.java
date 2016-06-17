package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.util.Arrays;
import java.util.Map;

/**
 * Base interface for a component.
 *
 * @author Petr Škoda
 */
public interface BaseComponent {

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
     * Report component initialization failure.
     */
    public class InitializationFailed extends NonRecoverableException {

        public InitializationFailed(String messages, Object... args) {
            super(Arrays.asList(new LocalizedException.Message(
                    messages, "en")), args);
        }

    }

    /**
     * Report component execution failure.
     */
    public static class ComponentFailed extends NonRecoverableException {

        public ComponentFailed(String messages, Object... args) {
            super(Arrays.asList(new LocalizedException.Message(
                    messages, "en")), args);
        }

    }

    public interface Context {

        public void sendMessage(Event message);

        /**
         *
         * @return True if component should stop (fail) as soon as possible.
         */
        public boolean canceled();

    }

    /**
     * Initialize component before execution.
     *
     * @param dataUnits
     * @param context
     * @throws BaseComponent.InitializationFailed
     */
    public void initialize(Map<String, DataUnit> dataUnits, Context context)
            throws InitializationFailed;

    /**
     *
     * @param key
     * @return Stored value or null.
     */
    public String getHeader(String key);

}
