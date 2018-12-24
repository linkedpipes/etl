package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.rdf4j.Statements;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Vocabulary;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a description of the component configuration.
 * For one only the first level of the configuration.
 */
class Description implements PojoLoader.Loadable {

    static final IRI TYPE;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        TYPE = valueFactory.createIRI(Vocabulary.CONFIG_DESCRIPTION);
    }

    public static class Member implements PojoLoader.Loadable {

        private IRI property;

        private IRI control;

        private boolean isPrivate = false;

        @Override
        public PojoLoader.Loadable load(String predicate, Value value) {
            switch (predicate) {
                case Vocabulary.CONFIG_DESC_PROPERTY:
                    this.property = (IRI) value;
                    break;
                case Vocabulary.CONFIG_DESC_CONTROL:
                    this.control = (IRI) value;
                    break;
                case Vocabulary.IS_PRIVATE:
                    this.isPrivate = ((Literal) value).booleanValue();
                    break;
                default:
                    break;
            }
            return null;
        }

        public IRI getProperty() {
            return property;
        }

        public IRI getControl() {
            return control;
        }

        public boolean isPrivate() {
            return isPrivate;
        }
    }

    /**
     * IRI of configuration object.
     */
    private IRI type;

    /**
     * Property used to control all properties, overwrite configuration
     * of all properties.
     */
    private IRI control;

    private List<Member> members = new ArrayList<>();

    @Override
    public PojoLoader.Loadable load(String predicate, Value value) {
        switch (predicate) {
            case Vocabulary.CONFIG_DESC_TYPE:
                this.type = (IRI) value;
                break;
            case Vocabulary.CONFIG_DESC_MEMBER:
                Member newMember = new Member();
                members.add(newMember);
                return newMember;
            case Vocabulary.CONFIG_DESC_CONTROL:
                this.control = (IRI) value;
                break;
            default:
                break;
        }
        return null;
    }

    public IRI getType() {
        return type;
    }

    public IRI getControl() {
        return control;
    }

    public List<Member> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public static Description fromStatements(Statements statements)
            throws BaseException {
        Description description = new Description();
        PojoLoader.loadOfType(statements, Description.TYPE, description);
        if (description.getType() == null) {
            throw new BaseException(
                    "Missing configuration type in description.");
        }
        return description;
    }

}
