package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;

class CreateNewConfiguration {

    private static final IRI CONTROL_NONE;

    private static final IRI CONTROL_INHERIT;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        CONTROL_NONE = valueFactory.createIRI(LP_OBJECTS.NONE);
        CONTROL_INHERIT = valueFactory.createIRI(LP_OBJECTS.INHERIT);
    }

    Collection<Statement> createNewFromJarFile(
            Collection<Statement> configurationRdf,
            Description description, String baseIri, IRI graph) {
        return createNew(configurationRdf, description,
                baseIri, graph, CONTROL_NONE);
    }

    Collection<Statement> createNewFromTemplate(
            Collection<Statement> configurationRdf,
            Description description, String baseIri, IRI graph) {
        return createNew(configurationRdf, description,
                baseIri, graph, CONTROL_INHERIT);
    }

    private Collection<Statement> createNew(
            Collection<Statement> configurationRdf, Description description,
            String baseIri, IRI graph, IRI control) {
        RdfObjects configuration = new RdfObjects(configurationRdf);
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
