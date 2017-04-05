package com.linkedpipes.etl.test.suite;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.LinkedList;
import java.util.List;

/**
 * Represent a description of a configuration object.
 */
class ConfigurationDescription implements Loadable {

    public static class Member implements Loadable {

        private final List<String> types = new LinkedList<>();

        private String iri;

        private String property;

        private String control;

        private boolean complex = false;

        public Member(String iri) {
            this.iri = iri;
        }

        @Override
        public Loadable load(String predicate, RdfValue object)
                throws RdfUtilsException {
            switch (predicate) {
                case RDF.TYPE:
                    types.add(object.asString());
                    break;
                case LP_OBJECTS.HAS_PROPERTY:
                    if (property != null) {
                        throw new RuntimeException("Multiple <" +
                                LP_OBJECTS.HAS_PROPERTY + "> values detected!");
                    }
                    property = object.asString();
                    break;
                case LP_OBJECTS.HAS_CONTROL:
                    if (control != null) {
                        throw new RuntimeException("Multiple <" +
                                LP_OBJECTS.HAS_CONTROL + "> values detected!");
                    }
                    control = object.asString();
                    break;
                case LP_OBJECTS.IS_COMPLEX:
                    if ("true".equalsIgnoreCase(object.asString())) {
                        complex = true;
                    }
                    break;

            }
            return null;
        }

        public void validate() throws InvalidDescription {
            if (!types.contains(LP_OBJECTS.MEMBER)) {
                throw new InvalidDescription("Missing type: {}", iri);
            }
            if (property == null) {
                throw new InvalidDescription("Missing property: {}", iri);
            }
            if (control == null) {
                throw new InvalidDescription("Missing control: {}", iri);
            }
        }

        public String getProperty() {
            return property;
        }

        public String getControl() {
            return control;
        }

        public boolean isComplex() {
            return complex;
        }

    }

    private final String iri;

    private final List<String> types = new LinkedList<>();

    private String referencedType;

    private final List<Member> members = new LinkedList<>();

    public ConfigurationDescription(String iri) {
        this.iri = iri;
    }

    @Override
    public Loadable load(String predicate, RdfValue object)
            throws RdfUtilsException {
        switch (predicate) {
            case RDF.TYPE:
                types.add(object.asString());
                break;
            case LP_OBJECTS.HAS_DESCRIBE:
                if (referencedType != null) {
                    throw new RuntimeException("Multiple <" +
                            LP_OBJECTS.HAS_DESCRIBE + "> values detected!");
                }
                referencedType = object.asString();
                break;
            case LP_OBJECTS.HAS_MEMBER:
                final Member newMember = new Member(object.asString());
                members.add(newMember);
                return newMember;
        }
        return null;
    }

    public void validate() throws InvalidDescription {
        if (!types.contains(LP_OBJECTS.DESCRIPTION)) {
            throw new InvalidDescription("Missing description type.");
        }
        if (referencedType == null) {
            throw new InvalidDescription(
                    "Missing referenced type predicate.");
        }
        for (Member member : members) {
            member.validate();
        }
    }

    public String getIri() {
        return iri;
    }

    public String getReferencedType() {
        return referencedType;
    }

    public Member getMember(String property) throws InvalidDescription {
        for (Member member : members) {
            if (member.property.equals(property)) {
                return member;
            }
        }
        throw new InvalidDescription("Missing description for <{}>", property);
    }

}
