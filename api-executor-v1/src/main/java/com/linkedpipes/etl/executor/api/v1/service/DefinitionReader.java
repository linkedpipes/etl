package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;

import java.util.Collection;

/**
 * Provide access to the component/pipeline definition.
 */
public interface DefinitionReader {

    Collection<String> getProperties(String property) throws LpException;

}
