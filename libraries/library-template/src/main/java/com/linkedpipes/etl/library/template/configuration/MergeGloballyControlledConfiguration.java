package com.linkedpipes.etl.library.template.configuration;

import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

class MergeGloballyControlledConfiguration {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public List<Statement> merge(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            ConfigurationDescription description, String baseIri
    ) throws ConfigurationException {
        Resource parentResource = findEntity(parentRdf, description);
        Resource instanceResource = findEntity(instanceRdf, description);
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

    private Resource findEntity(
            List<Statement> statements,
            ConfigurationDescription description) {
        for (Statement statement : statements) {
            if (!statement.getPredicate().equals(RDF.TYPE)) {
                continue;
            }
            if (statement.getObject().equals(
                    description.configurationType())) {
                return statement.getSubject();
            }
        }
        return null;
    }

    private String getGlobalControl(
            ConfigurationDescription description, List<Statement> statements,
            Resource resource) {
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            if (statement.getPredicate().equals(
                    description.globalControlProperty())) {
                return statement.getObject().stringValue();
            }
        }
        return LP_V1.NONE;
    }

    private List<Statement> mergeGlobal(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            ConfigurationDescription description,
            Resource parentResource,
            Resource instanceResource,
            String parentControl,
            String instanceControl,
            String baseIri) throws ConfigurationException {
        switch (parentControl) {
            case LP_V1.FORCE:
            case LP_V1.FORCED:
                return replaceControlAndSubject(
                        parentRdf,
                        parentResource,
                        description.globalControlProperty(),
                        LP_V1.FORCED,
                        baseIri);
            case LP_V1.NONE:
                break;
            case LP_V1.INHERIT_AND_FORCE:
            case LP_V1.INHERIT:
            default:
                throw new ConfigurationException(
                        "Invalid control: {} for: {}",
                        parentControl, parentResource);
        }
        switch (instanceControl) {
            case LP_V1.FORCE:
                return replaceControlAndSubject(
                        instanceRdf,
                        instanceResource,
                        description.globalControlProperty(),
                        LP_V1.FORCED,
                        baseIri);
            case LP_V1.NONE:
                return replaceControlAndSubject(
                        instanceRdf,
                        instanceResource,
                        description.globalControlProperty(),
                        LP_V1.NONE,
                        baseIri);
            case LP_V1.INHERIT_AND_FORCE:
                return replaceControlAndSubject(
                        parentRdf,
                        parentResource,
                        description.globalControlProperty(),
                        LP_V1.FORCED,
                        baseIri);
            case LP_V1.INHERIT:
                return replaceControlAndSubject(
                        parentRdf,
                        parentResource,
                        description.globalControlProperty(),
                        LP_V1.NONE,
                        baseIri);
            case LP_V1.FORCED:
            default:
                throw new ConfigurationException(
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
        StatementsUtils.renameSubject(
                result, resource,
                valueFactory.createIRI(baseIri + "/1"));
        return result;
    }

}
