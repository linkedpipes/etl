package com.linkedpipes.etl.storage.template.repository;

import com.linkedpipes.etl.storage.template.Template;

public interface RepositoryReference {

    public String getId();

    public Template.Type getType();

    public static RepositoryReference createJar(String id) {
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

    public static RepositoryReference createReference(String id) {
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
