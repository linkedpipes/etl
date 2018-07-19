package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.storage.BaseException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ConfigurationFacade {

    private final DescriptionLoader descriptionLoader = new DescriptionLoader();

    public Collection<Statement> createNewFromJarFile(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws BaseException {
        Description description = this.descriptionLoader.load(descriptionRdf);
        CreateNewConfiguration worker = new CreateNewConfiguration();
        return worker.createNewFromJarFile(
                configurationRdf, description, baseIri, graph);
    }

    public Collection<Statement> createNewFromTemplate(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws BaseException {
        Description description = this.descriptionLoader.load(descriptionRdf);
        CreateNewConfiguration worker = new CreateNewConfiguration();
        return worker.createNewFromTemplate(
                configurationRdf, description, baseIri, graph);
    }

    /**
     * Compute and return effective configuration for the given list of the
     * configuration, which must be sorted from parent to child.
     */
    public Collection<Statement> merge(
            Collection<Collection<Statement>> configurationsRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        Description description = this.descriptionLoader.load(descriptionRdf);
        MergeHierarchy worker = new MergeHierarchy();
        return worker.merge(configurationsRdf, description, baseIri, graph);
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
        Description description = this.descriptionLoader.load(descriptionRdf);
        MergeFromBottom worker = new MergeFromBottom();
        return worker.merge(
                templateRdf, instanceRdf, description, baseIri, graph);
    }

    /**
     * Select and return private configuration properties.
     */
    public Collection<Statement> selectPrivateStatements(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf) throws BaseException {
        Description description = this.descriptionLoader.load(descriptionRdf);

        SelectPrivateStatements worker = new SelectPrivateStatements();
        return worker.selectPrivate(configurationRdf, description);
    }

}
