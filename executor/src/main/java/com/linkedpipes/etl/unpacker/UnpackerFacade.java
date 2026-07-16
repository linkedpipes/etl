package com.linkedpipes.etl.unpacker;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.unpacker.model.GraphCollection;
import com.linkedpipes.etl.unpacker.model.ModelLoader;
import com.linkedpipes.etl.unpacker.model.designer.DesignerPipeline;
import com.linkedpipes.etl.unpacker.model.executor.ExecutorPipeline;
import com.linkedpipes.etl.unpacker.rdf.Loadable;
import java.util.Collection;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

/**
 * Provide capabilities to unpack pipeline for execution.
 */
public class UnpackerFacade {

    private final TemplateSource templateSource;

    private final ExecutionSource executionSource;

    public UnpackerFacade(TemplateSource templateSource, ExecutionSource executionSource) {
        this.templateSource = templateSource;
        this.executionSource = executionSource;
    }

    public Collection<Statement> unpack(Collection<Statement> pipelineRdf, Collection<Statement> optionsRdf)
            throws UnpackerException {
        UnpackOptions options = loadUnpackOptions(optionsRdf);
        StatementsSelector pipelineStatements = Statements.wrap(pipelineRdf).selector();
        DesignerPipeline pipeline = ModelLoader.loadDesignerPipeline(pipelineStatements);
        GraphCollection graphs = ModelLoader.loadConfigurationGraphs(pipelineStatements, pipeline);
        DesignerToExecutor designerToExecutor = new DesignerToExecutor(templateSource, executionSource);
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
