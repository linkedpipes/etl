package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;


public interface TemplateSource {

    Collection<Statement> getDefinition(String iri) throws StorageException;

    /**
     * Get template configuration.
     */
    Collection<Statement> getConfiguration(String iri) throws StorageException;

    Collection<Statement> getConfigurationDescription(String iri)
            throws StorageException;

}
