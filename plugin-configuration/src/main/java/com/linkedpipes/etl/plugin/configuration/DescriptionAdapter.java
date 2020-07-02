package com.linkedpipes.etl.plugin.configuration;

import com.linkedpipes.etl.model.vocabulary.LP;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

class DescriptionAdapter {

    static final IRI TYPE;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        TYPE = valueFactory.createIRI(LP.CONFIG_DESCRIPTION);
    }

    public Description fromStatements(List<Statement> statements)
            throws InvalidConfiguration {
        List<Resource> resources = findDescription(statements);
        if (resources.size() != 1) {
            throw new InvalidConfiguration(
                    "Invalid number of Description type resources ({}).",
                    resources.size());
        }
        return loadDescription(resources.get(0), statements);
    }

    private List<Resource> findDescription(List<Statement> statements) {
        List<Resource> result = new ArrayList<>();
        for (Statement statement : statements) {
            if (!statement.getPredicate().equals(RDF.TYPE)) {
                continue;
            }
            if (!statement.getObject().equals(TYPE)) {
                continue;
            }
            result.add(statement.getSubject());
        }
        return result;
    }

    private Description loadDescription(
            Resource resource, List<Statement> statements) {
        Description result = new Description();
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP.CONFIG_DESC_TYPE:
                    if (value instanceof IRI) {
                        result.setType((IRI) value);
                    }
                    break;
                case LP.CONFIG_DESC_MEMBER:
                    if (value instanceof Resource) {
                        result.getMembers().add(loadMember(
                                (Resource) value, statements));
                    }
                    break;
                case LP.CONFIG_DESC_CONTROL:
                    if (value instanceof IRI) {
                        result.setGlobalControl((IRI) value);
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    private Description.Member loadMember(
            Resource resource, List<Statement> statements) {
        Description.Member result = new Description.Member();
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP.CONFIG_DESC_PROPERTY:
                    if (value instanceof IRI) {
                        result.setProperty((IRI) value);
                    }
                    break;
                case LP.CONFIG_DESC_CONTROL:
                    if (value instanceof IRI) {
                        result.setControl((IRI) value);
                    }
                    break;
                case LP.IS_PRIVATE:
                    if (value instanceof Literal) {
                        result.setPrivate(((Literal) value).booleanValue());
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

}
