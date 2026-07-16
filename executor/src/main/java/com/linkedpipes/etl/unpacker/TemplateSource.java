package com.linkedpipes.etl.unpacker;

import java.util.Collection;
import org.eclipse.rdf4j.model.Statement;

/**
 * Provides RDF for a component's template: its definition, its default
 * configuration, and its configuration description.
 */
public interface TemplateSource {

    Collection<Statement> getDefinition(String iri) throws UnpackerException;

    Collection<Statement> getConfiguration(String iri) throws UnpackerException;

    Collection<Statement> getConfigurationDescription(String iri) throws UnpackerException;

}
