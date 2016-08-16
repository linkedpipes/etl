package com.linkedpipes.etl.component.api.service;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;

import java.util.Collection;

/**
 * Provide access to the component definition.
 *
 * @author Petr Škoda
 */
public interface DefinitionReader {

    /**
     * @param property
     * @return Values of given property on this component.
     */
    public Collection<String> getProperty(String property) throws LpException;

}
