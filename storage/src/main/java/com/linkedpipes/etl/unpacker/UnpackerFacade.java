package com.linkedpipes.etl.unpacker;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.unpacker.executions.ExecutionFacade;
import com.linkedpipes.etl.storage.pipeline.PipelineRef;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.unpacker.model.GraphCollection;
import com.linkedpipes.etl.unpacker.model.ModelLoader;
import com.linkedpipes.etl.unpacker.model.designer.DesignerPipeline;
import com.linkedpipes.etl.unpacker.model.executor.ExecutorPipeline;
import com.linkedpipes.etl.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Provide capabilities to unpack pipeline for execution.
 */
@Service
public class UnpackerFacade {

    @Autowired
    private PipelineFacade pipelines;

    @Autowired
    private TemplateFacade templates;

    @Autowired
    private ExecutionFacade executions;

    public Collection<Statement> unpack(
            PipelineRef pipeline, Collection<Statement> configurationRdf)
            throws StorageException {
        return unpack(pipelines.getPipelineRdf(pipeline), configurationRdf);
    }

    public Collection<Statement> unpack(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf) throws StorageException {
        UnpackOptions options = loadUnpackOptions(optionsRdf);
        StatementsSelector pipelineStatements =
                Statements.wrap(pipelineRdf).selector();
        DesignerPipeline pipeline =
                ModelLoader.loadDesignerPipeline(pipelineStatements);
        GraphCollection graphs =
                ModelLoader.loadConfigurationGraphs(
                        pipelineStatements, pipeline);
        DesignerToExecutor designerToExecutor =
                new DesignerToExecutor(templates, executions);
        designerToExecutor.transform(pipeline, graphs, options);

        ExecutorPipeline executorPipeline = designerToExecutor.getTarget();
        StatementsBuilder builder = Statements.arrayList().builder();
        executorPipeline.write(builder);
        for (String graph : executorPipeline.getReferencedGraphs()) {
            builder.addAll(graphs.get(graph));
        }
        return builder;
    }

    private UnpackOptions loadUnpackOptions(Collection<Statement> statements) {
        StatementsSelector selector = Statements.wrap(statements).selector();
        Collection<Resource> resources =
                selector.selectByType(UnpackOptions.TYPE).subjects();
        if (resources.isEmpty()) {
            // Return default as this is optional.
            return new UnpackOptions();
        }
        UnpackOptions result = new UnpackOptions();
        Loadable.load(selector, result, resources.iterator().next());
        return result;
    }

}
