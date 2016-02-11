package com.linkedpipes.etl.executor.api.v1.component;

/**
 * List of common headers that should be supported by every component.
 *
 * @author Škoda Petr
 */
public class Headers {

    private Headers() {
    }

    /**
     * Comma separated list of packages, that are considered to be part of a component. Only logs from these
     * packages are stored in a component log file.
     */
    public static final String LOG_PACKAGES = "log.packages";

}
