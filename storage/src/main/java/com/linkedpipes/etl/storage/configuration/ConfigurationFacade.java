package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.rdf4j.Statements;
import com.linkedpipes.etl.storage.BaseException;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ConfigurationFacade {

    public Statements createNewFromJarFile(
            Statements configurationRdf,
            Statements descriptionRdf,
            String baseIri, IRI graph)
            throws BaseException {
        Description description = Description.fromStatements(descriptionRdf);
        CreateNewConfiguration worker = new CreateNewConfiguration();
        return worker.createNewFromJarFile(
                configurationRdf, description, baseIri, graph);
    }

    public Statements createNewFromTemplate(
            Statements configurationRdf,
            Statements descriptionRdf,
            String baseIri, IRI graph)
            throws BaseException {
        Description description = Description.fromStatements(descriptionRdf);
        CreateNewConfiguration worker = new CreateNewConfiguration();
        return worker.createNewFromTemplate(
                configurationRdf, description, baseIri, graph);
    }

    /**
     * Compute and return effective configuration for the given list of the
     * configuration, which must be sorted from parent to child.
     */
    public Statements merge(
            Collection<Statements> configurationsRdf,
            Statements descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        Description description = Description.fromStatements(descriptionRdf);
        MergeHierarchy worker = new MergeHierarchy();
        return worker.merge(configurationsRdf, description, baseIri, graph);
    }

    /**
     * Designed to be used to merge configuration from instance to templates,
     * thus enabling another merge with other ancestor.
     */
    public Statements mergeFromBottom(
            Statements templateRdf,
            Statements instanceRdf,
            Statements descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        Description description = Description.fromStatements(descriptionRdf);
        MergeFromBottom worker = new MergeFromBottom();
        return worker.merge(
                templateRdf, instanceRdf, description, baseIri, graph);
    }

    /**
     * Select and return private configuration properties.
     */
    public Statements selectPrivateStatements(
            Statements rdf,
            Statements descriptionRdf) throws BaseException {
        Description description = Description.fromStatements(descriptionRdf);

        SelectPrivateStatements worker = new SelectPrivateStatements();
        return new Statements(worker.selectPrivate(rdf, description));
    }

    public Statements finalizeAfterMergeFromBottom(Statements configurationRdf) {
        MergeFromBottom worker = new MergeFromBottom();
        return worker.finalize(configurationRdf);
    }

}
