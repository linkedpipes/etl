package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.storage.BaseException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

public class ConfigurationFacade {

    private final CreateNewConfiguration createNewConfiguration =
            new CreateNewConfiguration();

    private final MergeHierarchy mergeHierarchy = new MergeHierarchy();

    private final MergeFromBottom mergeFromBottom = new MergeFromBottom();

    public Collection<Statement> createNewFromJarFile(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws BaseException {
        return createNewConfiguration.createNewFromJarFile(
                configurationRdf, descriptionRdf, baseIri, graph);
    }

    public Collection<Statement> createNewFromTemplate(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws BaseException {
        return createNewConfiguration.createNewFromTemplate(
                configurationRdf, descriptionRdf, baseIri, graph);
    }

    /**
     * Compute and return effective configuration for the given list of the
     * configuration, which must be sorted from parent to child.
     */
    public Collection<Statement> merge(
            Collection<Collection<Statement>> configurationsRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        return mergeHierarchy.merge(
                configurationsRdf, descriptionRdf, baseIri, graph);
    }


    /**
     * Designed to be used to merge configuration from instance to templates,
     * thus enabling another merge with other ancestor.
     */
    public Collection<Statement> mergeFromBottom(
            Collection<Statement> templateRdf,
            Collection<Statement> instanceRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        return mergeFromBottom.merge(
                templateRdf, instanceRdf, descriptionRdf, baseIri, graph);
    }

}
