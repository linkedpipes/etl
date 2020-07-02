package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationFacade {

    private DescriptionAdapter descriptionAdapter = new DescriptionAdapter();

    public List<Statement> createNewFromJarFile(
            List<Statement> configurationRdf, List<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws InvalidConfiguration {
        Description description = loadDescription(descriptionRdf);
        CreateNewConfiguration worker = new CreateNewConfiguration();
        return worker.createNewFromJarFile(
                configurationRdf, description, baseIri, graph);
    }

    private Description loadDescription(List<Statement> statements)
            throws InvalidConfiguration {
        return descriptionAdapter.fromStatements(statements);
    }

    public List<Statement> createNewFromTemplate(
            List<Statement> configurationRdf, List<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws InvalidConfiguration {
        Description description = loadDescription(descriptionRdf);
        CreateNewConfiguration worker = new CreateNewConfiguration();
        return worker.createNewFromTemplate(
                configurationRdf, description, baseIri, graph);
    }

    /**
     * The configurationsRdf must start with the template.
     */
    public List<Statement> merge(
            List<List<Statement>> configurationsRdf,
            List<Statement> descriptionRdf,
            String baseIri, IRI graph) throws InvalidConfiguration {
        Description description = loadDescription(descriptionRdf);
        MergeConfiguration mergeConfiguration = new MergeConfiguration();
        List<Statement> result = new ArrayList<>(configurationsRdf.get(0));
        for (int index = 1; index < configurationsRdf.size(); ++index) {
            result = mergeConfiguration.merge(
                    result,
                    configurationsRdf.get(index),
                    description,
                    baseIri,
                    graph
            );
        }
        return result;
    }

    /**
     * Return private configuration properties.
     */
    public List<Statement> selectPrivateStatements(
            List<Statement> configurationRdf,
            List<Statement> descriptionRdf) throws InvalidConfiguration {
        Description description = loadDescription(descriptionRdf);
        SelectPrivateStatements selectPrivate = new SelectPrivateStatements();
        return selectPrivate.selectPrivate(configurationRdf, description);
    }

}
