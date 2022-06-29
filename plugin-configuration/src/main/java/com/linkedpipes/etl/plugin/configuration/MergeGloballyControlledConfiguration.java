package com.linkedpipes.etl.plugin.configuration;

import com.linkedpipes.etl.model.vocabulary.LP;
import com.linkedpipes.etl.plugin.configuration.model.Description;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

class MergeGloballyControlledConfiguration {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public List<Statement> merge(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            Description description, String baseIri
    ) throws InvalidConfiguration {
        Resource parentResource = RdfUtils.findByType(
                parentRdf, description.getType());
        Resource instanceResource = RdfUtils.findByType(
                instanceRdf, description.getType());
        String parentControl =
                getGlobalControl(description, parentRdf, parentResource);
        String instanceControl =
                getGlobalControl(description, instanceRdf, instanceResource);
        //
        return mergeGlobal(
                parentRdf, instanceRdf, description,
                parentResource, instanceResource,
                parentControl, instanceControl, baseIri);
    }

    private String getGlobalControl(
            Description description, List<Statement> statements,
            Resource resource) {
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            if (statement.getPredicate().equals(
                    description.getGlobalControl())) {
                return statement.getObject().stringValue();
            }
        }
        return LP.NONE;
    }

    private List<Statement> mergeGlobal(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            Description description,
            Resource parentResource,
            Resource instanceResource,
            String parentControl,
            String instanceControl,
            String baseIri) throws InvalidConfiguration {
        switch (parentControl) {
            case LP.FORCE:
            case LP.FORCED:
                return replaceControlAndSubject(
                        parentRdf,
                        parentResource,
                        description.getGlobalControl(),
                        LP.FORCED,
                        baseIri);
            case LP.NONE:
                break;
            case LP.INHERIT_AND_FORCE:
            case LP.INHERIT:
            default:
                throw new InvalidConfiguration(
                        "Invalid control: {} for: {}",
                        parentControl, parentResource);
        }
        switch (instanceControl) {
            case LP.FORCE:
                return replaceControlAndSubject(
                        instanceRdf,
                        instanceResource,
                        description.getGlobalControl(),
                        LP.FORCED,
                        baseIri);
            case LP.NONE:
                return replaceControlAndSubject(
                        instanceRdf,
                        instanceResource,
                        description.getGlobalControl(),
                        LP.NONE,
                        baseIri);
            case LP.INHERIT_AND_FORCE:
                return replaceControlAndSubject(
                        parentRdf,
                        parentResource,
                        description.getGlobalControl(),
                        LP.FORCED,
                        baseIri);
            case LP.INHERIT:
                return replaceControlAndSubject(
                        parentRdf,
                        parentResource,
                        description.getGlobalControl(),
                        LP.NONE,
                        baseIri);
            case LP.FORCED:
            default:
                throw new InvalidConfiguration(
                        "Invalid control: {} for: {}",
                        parentControl, parentResource);
        }
    }


    private List<Statement> replaceControlAndSubject(
            List<Statement> statements, Resource resource, IRI predicate,
            String control, String baseIri) {
        List<Statement> result = new ArrayList<>(statements);
        result.removeIf(statement ->
                statement.getSubject().equals(resource)
                        && statement.getPredicate().equals(predicate));
        result.add(valueFactory.createStatement(
                resource, predicate, valueFactory.createIRI(control)));
        RdfUtils.updateSubject(
                result, resource,
                valueFactory.createIRI(baseIri + "/1"));
        return result;
    }

}
