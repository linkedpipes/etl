package com.linkedpipes.etl.storage.unpacker.model;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerComponent;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerConnection;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerPipeline;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerRunAfter;
import com.linkedpipes.etl.storage.unpacker.model.execution.Execution;
import com.linkedpipes.etl.storage.unpacker.model.template.JarTemplate;
import com.linkedpipes.etl.storage.unpacker.model.template.ReferenceTemplate;
import com.linkedpipes.etl.storage.unpacker.model.template.Template;
import com.linkedpipes.etl.storage.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModelLoader {

    private ModelLoader() {

    }

    public static DesignerPipeline loadDesignerPipeline(Statements source)
            throws StorageException {
        StatementsSelector selector = source.selector();
        Collection<Resource> candidatePipelines =
                selector.selectByType(DesignerPipeline.TYPE).subjects();
        if (candidatePipelines.size() != 1) {
            throw new StorageException("Invalid number of pipelines '{}'.",
                    candidatePipelines.size());
        }
        DesignerPipeline pipelineModel = new DesignerPipeline();
        Loadable.load(
                selector, pipelineModel,
                candidatePipelines.iterator().next());
        for (Resource subject :
                selector.selectByType(DesignerConnection.TYPE).subjects()) {
            DesignerConnection connection = new DesignerConnection();
            Loadable.load(selector, connection, subject);
            pipelineModel.getConnections().add(connection);
        }
        for (Resource subject :
                selector.selectByType(DesignerRunAfter.TYPE).subjects()) {
            DesignerRunAfter connection = new DesignerRunAfter();
            Loadable.load(selector, connection, subject);
            pipelineModel.getRunAfter().add(connection);
        }
        for (Resource subject :
                selector.selectByType(DesignerComponent.TYPE).subjects()) {
            DesignerComponent component = new DesignerComponent();
            Loadable.load(selector, component, subject);
            pipelineModel.getComponents().add(component);
        }
        return pipelineModel;
    }

    public static GraphCollection loadConfigurationGraphs(
            Statements source, DesignerPipeline pipeline) {
        GraphCollection collection = new GraphCollection();
        for (DesignerComponent component : pipeline.getComponents()) {
            for (String graph : component.getConfigurationGraphs()) {
                collection.put(graph, extractGraph(source, graph));
            }
        }
        return collection;
    }

    public static Execution loadExecution(Statements source)
            throws StorageException {
        List<Resource> resources = new ArrayList<>(
                source.selector().selectByType(Execution.TYPE)
                        .subjects());
        if (resources.size() != 1) {
            throw new StorageException(
                    "Invalid count of executions '{}'.",
                    resources.size());
        }
        Execution execution = new Execution();
        Loadable.load(source.selector(), execution, resources.get(0));
        return execution;
    }

    private static Collection<Statement> extractGraph(
            Statements source, String graph) {
        return source.selector().selectByGraph(graph);
    }

    public static Template loadTemplate(Statements source)
            throws StorageException {
        List<Resource> jar = new ArrayList<>(
                source.selector().selectByType(JarTemplate.TYPE)
                        .subjects());
        if (jar.size() == 1) {
            JarTemplate template = new JarTemplate();
            Loadable.load(source.selector(), template, jar.get(0));
            return template;
        } else if (jar.size() > 1) {
            throw new StorageException(
                    "Invalid count of jar templates: {}",
                    jar.size());
        }
        List<Resource> reference = new ArrayList<>(
                source.selector().selectByType(ReferenceTemplate.TYPE)
                        .subjects());
        if (reference.size() == 1) {
            ReferenceTemplate template = new ReferenceTemplate();
            Loadable.load(source.selector(), template, reference.get(0));
            return template;
        } else if (reference.size() > 1) {
            throw new StorageException(
                    "Invalid count of reference templates: {}",
                    reference.size());
        }
        throw new StorageException(
                "Invalid count of templates: {} (jar) {} (reference)",
                jar.size(), reference.size());
    }

}
