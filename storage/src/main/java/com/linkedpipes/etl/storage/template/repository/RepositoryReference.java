package com.linkedpipes.etl.storage.template.repository;

import com.linkedpipes.etl.storage.template.Template;

public interface RepositoryReference {

    String JAR_PREFIX = "jar-";

    String getId();

    Template.Type getType();

    static boolean isJarId(String directory) {
        return directory.startsWith(JAR_PREFIX);
    }

    static RepositoryReference createJar(String iri) {
        return new RepositoryReference() {
            @Override
            public String getId() {
                // http://etl.linkedpipes.com/resources/components/e-httpGetFiles/0.0.0
                int start = iri.indexOf(":") + 3;
                return JAR_PREFIX + iri.substring(start).replaceAll("/", "-");
            }

            @Override
            public Template.Type getType() {
                return Template.Type.JAR_TEMPLATE;
            }
        };
    }

    static RepositoryReference createJarFromId(String id) {
        return new RepositoryReference() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public Template.Type getType() {
                return Template.Type.JAR_TEMPLATE;
            }
        };
    }

    static RepositoryReference createReference(String iri) {
        return new RepositoryReference() {
            @Override
            public String getId() {
                return iri.substring(iri.lastIndexOf("/"));
            }

            @Override
            public Template.Type getType() {
                return Template.Type.REFERENCE_TEMPLATE;
            }
        };
    }

    static RepositoryReference createReferenceFromId(String id) {
        return new RepositoryReference() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public Template.Type getType() {
                return Template.Type.REFERENCE_TEMPLATE;
            }
        };
    }

}
