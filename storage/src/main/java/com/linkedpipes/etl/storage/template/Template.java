package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.template.repository.RepositoryReference;

public abstract class Template implements RepositoryReference {

    public enum Type {
        JAR_TEMPLATE,
        REFERENCE_TEMPLATE
    }

    protected String id;

    protected String iri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    /**
     * Return true if component support configuration control/inheritance.
     */
    public abstract boolean isSupportingControl();

    public  abstract String getConfigurationDescription();

}
