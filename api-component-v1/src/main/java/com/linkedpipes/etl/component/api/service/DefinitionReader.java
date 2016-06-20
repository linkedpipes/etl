package com.linkedpipes.etl.component.api.service;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.util.Collection;

/**
 * Provide access to the pipeline definition.
 *
 * @author Petr Škoda
 */
public interface DefinitionReader {

    public Collection<String> getProperty(String property) throws LpException;

}
