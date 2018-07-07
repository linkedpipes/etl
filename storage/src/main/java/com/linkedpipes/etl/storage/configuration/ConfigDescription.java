package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.storage.Vocabulary;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.eclipse.rdf4j.model.IRI;
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
class ConfigDescription implements PojoLoader.Loadable {

    static final IRI TYPE;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        TYPE = valueFactory.createIRI(Vocabulary.CONFIG_DESCRIPTION);
    }

    public static class Member implements PojoLoader.Loadable {

        private IRI property;

        private IRI control;

        @Override
        public PojoLoader.Loadable load(String predicate, Value value) {
            switch (predicate) {
                case Vocabulary.CONFIG_DESC_PROPERTY:
                    property = (IRI) value;
                    break;
                case Vocabulary.CONFIG_DESC_CONTROL:
                    control = (IRI) value;
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

}
