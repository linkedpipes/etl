package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;

class CreateNewConfiguration {

    private final DescriptionLoader descriptionLoader = new DescriptionLoader();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    Collection<Statement> createNewFromJarFile(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws BaseException {
        return createNew(configurationRdf, descriptionRdf,
                baseIri, graph, valueFactory.createIRI(LP_OBJECTS.NONE));
    }

    Collection<Statement> createNewFromTemplate(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph)
            throws BaseException {
        return createNew(configurationRdf, descriptionRdf,
                baseIri, graph, valueFactory.createIRI(LP_OBJECTS.INHERIT));
    }

    private Collection<Statement> createNew(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph, IRI control) throws BaseException {
        Description description =
                descriptionLoader.load(descriptionRdf);
        RdfObjects configuration = new RdfObjects(configurationRdf);
        // Update configuration.
        configuration.getTyped(description.getType()).forEach((instance) -> {
            for (Description.Member member : description.getMembers()) {
                instance.deleteReferences(member.getControl());
                instance.add(member.getControl(), control);
            }
        });
        configuration.updateTypedResources(baseIri);
        return configuration.asStatements(graph);
    }

}
