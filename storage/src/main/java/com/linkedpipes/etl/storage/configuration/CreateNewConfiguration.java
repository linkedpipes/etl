package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.rdf4j.Statements;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

class CreateNewConfiguration {

    private static final IRI CONTROL_NONE;

    private static final IRI CONTROL_INHERIT;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        CONTROL_NONE = valueFactory.createIRI(LP_OBJECTS.NONE);
        CONTROL_INHERIT = valueFactory.createIRI(LP_OBJECTS.INHERIT);
    }

    public Statements createNewFromJarFile(
            Statements configurationRdf,
            Description description,
            String baseIri, IRI graph) {
        return createNew(configurationRdf, description,
                baseIri, graph, CONTROL_NONE);
    }

    public Statements createNewFromTemplate(
            Statements configurationRdf,
            Description description,
            String baseIri, IRI graph) {
        return createNew(configurationRdf, description,
                baseIri, graph, CONTROL_INHERIT);
    }

    private Statements createNew(
            Statements configurationRdf,
            Description description,
            String baseIri, IRI graph,
            IRI defaultControl) {
        RdfObjects configuration = new RdfObjects(configurationRdf);
        replaceControl(configuration, description, defaultControl);
        configuration.updateTypedResources(baseIri);
        return new Statements(configuration.asStatements(graph));
    }

    private void replaceControl(
            RdfObjects configuration, Description description, IRI control) {
        configuration.getTyped(description.getType()).forEach((instance) -> {
            for (Description.Member member : description.getMembers()) {
                instance.deleteReferences(member.getControl());
                instance.add(member.getControl(), control);
            }
        });
    }

}
