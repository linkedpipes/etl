package com.linkedpipes.etl.storage.component.pipeline;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.openrdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Petr Škoda
 */
@Service
public class PipelineFacade {

    /**
     * Public exception used by this facade to report problems.
     */
    public static class OperationFailed extends BaseException {

        OperationFailed(Throwable cause) {
            super(cause);
        }
    }

    @Autowired
    private PipelineManager pipelines;

    public Pipeline getPipeline(String iri) {
        return pipelines.getPipelines().get(iri);
    }

    public Collection<Statement> getReferenceRdf() {
        final Collection<Statement> result = new LinkedList<>();
        for (Pipeline pipeline : pipelines.getPipelines().values()) {
            result.addAll(pipeline.getReferenceRdf());
        }
        return result;
    }

    public Collection<Statement> getReferenceRdf(Pipeline pipeline) {
        return pipeline.getReferenceRdf();
    }

    /**
     * Return an RDF definition of given pipeline.
     *
     * @param pipeline
     * @return
     */
    public Collection<Statement> getPipelineRdf(Pipeline pipeline)
            throws OperationFailed {
        final File pipelineFile = pipeline.getFile();
        // TODO If we have the file in same format as required we could stream it.
        try {
            return RdfUtils.read(pipelineFile);
        } catch (Exception ex) {
            throw new OperationFailed(ex);
        }
    }

    /**
     * @param pipelineRdf If empty an "empty pipeline" is used instead.
     * @param optionsRdf
     * @return
     */
    public Pipeline createPipeline(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf)
            throws BaseException {
        final Pipeline pipeline;
        try {
            pipeline = pipelines.createPipeline();
        } catch (Exception ex) {
            throw new OperationFailed(ex);
        }

        // Make sure we have the definition loaded.
        if (pipelineRdf.isEmpty()) {
            pipelineRdf = getPipelineRdf(pipeline);
        }
        // Perform modifications based on the options.
        final UpdateOptions options = new UpdateOptions();
        PojoLoader.loadOfType(optionsRdf, UpdateOptions.TYPE, options);
        pipelineRdf = PipelineUpdater.update(pipeline, options, pipelineRdf);
        // Set pipeline content.
        try {
            pipelines.updatePipeline(pipeline, pipelineRdf);
        } catch (Exception ex) {
            throw new OperationFailed(ex);
        }
        return pipeline;
    }

    /**
     * Update pipeline from definition.
     *
     * @param pipeline
     * @param pipelineRdf
     */
    public void updatePipeline(Pipeline pipeline,
            Collection<Statement> pipelineRdf) throws OperationFailed {
        try {
            pipelines.updatePipeline(pipeline, pipelineRdf);
        } catch (Exception ex) {
            throw new OperationFailed(ex);
        }
    }

    /**
     * Delete given pipeline.
     *
     * @param pipeline
     */
    public void deletePipeline(Pipeline pipeline) {
        pipelines.deletePipeline(pipeline);
    }

}
