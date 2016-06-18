package com.linkedpipes.etl.component.api.service;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 * Can be used to create an exception that reports various failures.
 *
 * @author Petr Škoda
 */
public interface ExceptionFactory {

    /**
     * Used for general exceptions. Use "{}" in the message to refer to
     * arguments.
     *
     * @param message
     * @param args
     * @return
     */
    public LpException failed(String message, Object... args);

    /**
     * Used to report general problem with certain configuration property.
     *
     * @param propertyIri
     * @param message
     * @param args
     * @return
     */
    public LpException invalidConfigurationProperty(String propertyIri,
            String message, Object... args);

    /**
     * Used to report missing or empty value in configuration where
     * it is not expected.
     *
     * @param propertyIri
     * @return
     */
    public LpException missingConfigurationProperty(String propertyIri);

}
