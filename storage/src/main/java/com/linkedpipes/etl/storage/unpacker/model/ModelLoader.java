package com.linkedpipes.etl.storage.unpacker.model;

import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerComponent;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerConnection;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerPipeline;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerRunAfter;
import com.linkedpipes.etl.storage.unpacker.model.execution.Execution;
import com.linkedpipes.etl.storage.unpacker.model.template.JarTemplate;
import com.linkedpipes.etl.storage.unpacker.model.template.ReferenceTemplate;
import com.linkedpipes.etl.storage.unpacker.model.template.Template;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModelLoader {

    private ModelLoader() {

    }

    public static DesignerPipeline loadDesignerPipeline(BackendRdfSource source)
            throws RdfUtilsException {
        DesignerPipeline pipelineModel = new DesignerPipeline();
        RdfUtils.loadByType(source, null, pipelineModel, DesignerPipeline.TYPE);

        pipelineModel.getConnections().addAll((RdfUtils.loadList(source, null,
                DesignerConnection.class, DesignerConnection.TYPE)));

        pipelineModel.getRunAfter().addAll((RdfUtils.loadList(source, null,
                DesignerRunAfter.class, DesignerRunAfter.TYPE)));

        pipelineModel.getComponents().addAll((RdfUtils.loadList(source, null,
                DesignerComponent.class, DesignerComponent.TYPE)));

        return pipelineModel;
    }

    public static GraphCollection loadConfigurationGraphs(
            Rdf4jSource source, DesignerPipeline pipeline)
            throws RdfUtilsException {
        GraphCollection collection = new GraphCollection();
        for (DesignerComponent component : pipeline.getComponents()) {
            for (String graph : component.getConfigurationGraphs()) {
                collection.put(graph, extractGraph(source, graph));
            }
        }
        return collection;
    }

    public static Execution loadExecution(BackendRdfSource source)
            throws RdfUtilsException {
        Execution execution = new Execution();
        RdfUtils.loadByType(source, null, execution, Execution.TYPE);
        return execution;
    }

    private static Collection<Statement> extractGraph(
            Rdf4jSource source, String graph) throws RdfUtilsException {
        List<Statement> statements = new ArrayList<>();
        source.statements(graph, statement -> statements.add(statement));
        return statements;
    }

    public static Template loadTemplate(BackendRdfSource source)
            throws RdfUtilsException {
        List<String> jar = RdfUtils.getResourcesOfType(
                source, null, JarTemplate.TYPE);
        if (jar.size() == 1) {
            JarTemplate template = new JarTemplate();
            RdfUtils.load(source, jar.get(0), null, template);
            return template;
        } else if (jar.size() > 1) {
            throw new RdfUtilsException(
                    "Invalid count of jar templates: {}",
                    jar.size());
        }
        List<String> reference = RdfUtils.getResourcesOfType(
                source, null, ReferenceTemplate.TYPE);
        if (reference.size() == 1) {
            ReferenceTemplate template = new ReferenceTemplate();
            RdfUtils.load(source, reference.get(0), null, template);
            return template;
        } else if (reference.size() > 1) {
            throw new RdfUtilsException(
                    "Invalid count of reference templates: {}",
                    reference.size());
        }
        throw new RdfUtilsException(
                "Invalid count of templates: {} (jar) {} (reference)",
                jar.size(), reference.size());
    }

}
