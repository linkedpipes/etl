package com.linkedpipes.etl.component.api.service;

import java.util.Collection;

/**
 * Provide access to the pipeline definition.
 *
 * @author Petr Škoda
 */
public interface DefinitionReader {

    public static class OperationFailed extends Exception {

        public OperationFailed(String message) {
            super(message);
        }

        public OperationFailed(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     *
     * @param property
     * @return Value of component definition property.
     * @throws OperationFailed
     */
    public Collection<String> getProperty(String property)
            throws OperationFailed;

}
