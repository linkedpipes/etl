package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a description of the configuration. For one only the first
 * level of the configuration.
 *
 * @author Petr Škoda
 */
class ConfigDescription implements PojoLoader.Loadable {

    static final IRI TYPE;

    static {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        TYPE = vf.createIRI(
                "http://plugins.linkedpipes.com/ontology/ConfigurationDescription");
    }

    public static class Member implements PojoLoader.Loadable {

        private IRI property;

        private IRI control;

        @Override
        public PojoLoader.Loadable load(String predicate, Value value)
                throws PojoLoader.CantLoadException {
            switch (predicate) {
                case "http://plugins.linkedpipes.com/ontology/configuration/property":
                    property = (IRI) value;
                    break;
                case "http://plugins.linkedpipes.com/ontology/configuration/control":
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
     * Type of configuration object.
     */
    private IRI type;

    /**
     * Property used to control all properties, overwrite configuration
     * of all properties.
     */
    private IRI control;

    private List<Member> members = new ArrayList<>(12);

    @Override
    public PojoLoader.Loadable load(String predicate, Value value)
            throws PojoLoader.CantLoadException {
        switch (predicate) {
            case "http://plugins.linkedpipes.com/ontology/configuration/type":
                this.type = (IRI) value;
                break;
            case "http://plugins.linkedpipes.com/ontology/configuration/member":
                final Member newMember = new Member();
                members.add(newMember);
                return newMember;
            case "http://plugins.linkedpipes.com/ontology/configuration/control":
                this.control = (IRI)value;
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
