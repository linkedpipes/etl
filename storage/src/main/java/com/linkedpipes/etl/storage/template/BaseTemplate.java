package com.linkedpipes.etl.storage.template;

import org.openrdf.model.Statement;

import java.io.File;
import java.util.Collection;

abstract class BaseTemplate implements Template {

    protected String iri;

    protected File directory;

    /**
     * Component interface.
     */
    protected Collection<Statement> interfaceRdf;

    /**
     * Definition of the component.
     */
    protected Collection<Statement> definitionRdf;

    /**
     * Component configuration.
     */
    protected Collection<Statement> configRdf;

    /**
     * Component configuration description.
     */
    protected Collection<Statement> configDescRdf;

    /**
     * Component used for new instances of this template.
     */
    protected Collection<Statement> configForInstanceRdf;

    protected BaseTemplate() {

    }

    public abstract boolean isSupportControl();

    @Override
    public String getIri() {
        return iri;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public Collection<Statement> getInterfaceRdf() {
        return interfaceRdf;
    }

    public void setInterfaceRdf(
            Collection<Statement> interfaceRdf) {
        this.interfaceRdf = interfaceRdf;
    }

    public Collection<Statement> getDefinitionRdf() {
        return definitionRdf;
    }

    public void setDefinitionRdf(
            Collection<Statement> definitionRdf) {
        this.definitionRdf = definitionRdf;
    }

    public Collection<Statement> getConfigRdf() {
        return configRdf;
    }

    public void setConfigRdf(Collection<Statement> configRdf) {
        this.configRdf = configRdf;
    }

    public Collection<Statement> getConfigDescRdf() {
        return configDescRdf;
    }

    public void setConfigDescRdf(
            Collection<Statement> configDescRdf) {
        this.configDescRdf = configDescRdf;
    }

    public Collection<Statement> getConfigForInstanceRdf() {
        return configForInstanceRdf;
    }

    public void setConfigForInstanceRdf(
            Collection<Statement> configForInstanceRdf) {
        this.configForInstanceRdf = configForInstanceRdf;
    }
}
