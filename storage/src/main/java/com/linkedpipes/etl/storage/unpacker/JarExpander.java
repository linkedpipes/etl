package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.unpacker.model.GraphCollection;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorComponent;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorPort;
import com.linkedpipes.etl.storage.unpacker.model.template.JarTemplate;
import com.linkedpipes.etl.storage.unpacker.model.template.TemplatePort;

import java.util.Arrays;
import java.util.List;

class JarExpander {

    private GraphCollection graphs;

    private ExecutorComponent component;

    private final TemplateSource templateSource;

    public JarExpander(TemplateSource templateSource) {
        this.templateSource = templateSource;
    }

    public ExecutorComponent expand(
            String iri, List<String> configurations,
            JarTemplate template) throws BaseException {

        component = new ExecutorComponent();
        component.setIri(iri);
        component.setConfigGraph(iri + "/configuration");

        copyTemplate(template);

        mergeConfigurations(template, configurations);


        return component;
    }

    private void copyTemplate(JarTemplate template) {
        component.setJar(template.getJar());
        component.setRequirements(template.getRequirements());
        component.setTypes(Arrays.asList(LP_PIPELINE.COMPONENT));
        component.setConfigDescriptionGraph(
                template.getConfigDescriptionGraph());
        copyPorts(template);
    }

    private void copyPorts(JarTemplate template) {
        for (TemplatePort port : template.getPorts()) {
            component.addPort(instantiatePort(port));
        }
    }

    private ExecutorPort instantiatePort(TemplatePort port) {
        ExecutorPort newPort = new ExecutorPort();

        newPort.setIri(component.getIri() + "/port/" + port.getBinding());
        newPort.setBinding(port.getBinding());
        newPort.setRequirements(port.getRequirements());
        newPort.setTypes(port.getTypes());

        return newPort;
    }

    private void mergeConfigurations(
            JarTemplate template,
            List<String> configurations) throws BaseException {
        if (configurations.isEmpty()) {
            if (template.getConfigGraph() != null) {
                copyConfigurationFromTemplate(template);
            }
        } else {
            mergeWithTemplate(template, configurations);
        }
    }

    private void copyConfigurationFromTemplate(JarTemplate template)
            throws BaseException {
        String configIri = component.getIri() + "/configuration";
        component.setConfigGraph(configIri);

        ConfigurationMerger merger = createMerger(template);
        merger.copyConfigurationGraphs(
                template.getConfigGraph(), configIri);
    }

    private ConfigurationMerger createMerger(JarTemplate template)
            throws BaseException {
        ConfigurationMerger merger = new ConfigurationMerger(
                graphs, templateSource);
        merger.loadTemplateConfigAndDescription(template);
        return merger;
    }

    private void mergeWithTemplate(
            JarTemplate template, List<String> configurations)
            throws BaseException {
        ConfigurationMerger merger = createMerger(template);
        configurations.add(template.getConfigGraph());
        merger.merge(template, configurations, component.getConfigGraph());
    }

    public void setGraphs(GraphCollection graphs) {
        this.graphs = graphs;
    }

}
