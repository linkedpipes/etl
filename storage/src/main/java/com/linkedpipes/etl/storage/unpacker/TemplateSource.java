package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.BaseException;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

public interface TemplateSource {

    Collection<Statement> getDefinition(String iri) throws BaseException;

    /**
     * Get template configuration.
     */
    Collection<Statement> getConfiguration(String iri) throws BaseException;

    Collection<Statement> getConfigurationDescription(String iri)
            throws BaseException;

}
