package com.linkedpipes.etl.plugin.configuration.model;

import org.eclipse.rdf4j.model.IRI;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a description of the component configuration.
 * For one only the first level of the configuration.
 */
public class Description {

    public static class Member {

        private IRI property;

        private IRI control;

        private IRI substitution;

        private boolean isPrivate = false;

        public IRI getProperty() {
            return property;
        }

        public void setProperty(IRI property) {
            this.property = property;
        }

        public IRI getControl() {
            return control;
        }

        public void setControl(IRI control) {
            this.control = control;
        }

        public boolean isPrivate() {
            return isPrivate;
        }

        public void setPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
        }

        public IRI getSubstitution() {
            return substitution;
        }

        public void setSubstitution(IRI substitution) {
            this.substitution = substitution;
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
    private IRI globalControl;

    private final List<Member> members = new ArrayList<>();

    public IRI getType() {
        return type;
    }

    public void setType(IRI type) {
        this.type = type;
    }

    public IRI getGlobalControl() {
        return globalControl;
    }

    public void setGlobalControl(IRI globalControl) {
        this.globalControl = globalControl;
    }

    public List<Member> getMembers() {
        return members;
    }

}
