package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.rdf.utils.InvalidNumberOfResults;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.rdf4j.ClosableRdf4jSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.StatementsCollector;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.executions.ExecutionFacade;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.unpacker.model.GraphCollection;
import com.linkedpipes.etl.storage.unpacker.model.ModelLoader;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerPipeline;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorPipeline;
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
            Pipeline pipeline, Collection<Statement> configurationRdf)
            throws BaseException {
        return unpack(pipelines.getPipelineRdf(pipeline), configurationRdf);
    }

    public Collection<Statement> unpack(
            Collection<Statement> pipelineRdf,
            Collection<Statement> configurationRdf) throws BaseException {
        UnpackOptions options = new UnpackOptions();
        ClosableRdf4jSource optionsSource = Rdf4jSource.wrapInMemory(
                configurationRdf);
        try {
            RdfUtils.loadByType(optionsSource, null, options,
                    UnpackOptions.TYPE);
        } catch (InvalidNumberOfResults ex) {
            // Ignore as the option is optional.
        } catch (RdfUtilsException ex) {
            throw new BaseException("Can't load execution options.", ex);
        } finally {
            optionsSource.close();
        }

        ClosableRdf4jSource source = Rdf4jSource.wrapInMemory(pipelineRdf);
        DesignerPipeline pipeline;
        GraphCollection graphs;
        try {
            pipeline = ModelLoader.loadDesignerPipeline(source);
            graphs = ModelLoader.loadConfigurationGraphs(source, pipeline);
        } catch (RdfUtilsException ex) {
            throw new BaseException("Can't unpack pipeline.", ex);
        } finally {
            source.close();
        }

        DesignerToExecutor designerToExecutor =
                new DesignerToExecutor(templates, executions);
        designerToExecutor.transform(pipeline, graphs, options);

        ExecutorPipeline executorPipeline = designerToExecutor.getTarget();

        StatementsCollector collector = new StatementsCollector(
                executorPipeline.getIri());
        executorPipeline.write(collector);
        for (String graph : executorPipeline.getReferencedGraphs()) {
            graphs.get(graph).forEach((statement -> {
                collector.add(statement);
            }));
        }
        return collector.getStatements();
    }

}
