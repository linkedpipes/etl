package com.linkedpipes.etl.storage.template.repository;

import com.linkedpipes.etl.storage.template.Template;

public interface RepositoryReference {

    String getId();

    Template.Type getType();

    static RepositoryReference createJar(String id) {
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

    static RepositoryReference createReference(String id) {
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
