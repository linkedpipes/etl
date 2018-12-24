package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

@Service
public class PipelineFacade {

    private final PipelineManager pipelines;

    @Autowired
    public PipelineFacade(PipelineManager pipelines) {
        this.pipelines = pipelines;
    }

    public Pipeline getPipeline(String iri) {
        return pipelines.getPipelines().get(iri);
    }

    public Collection<Statement> getReferenceAsRdf() {
        Collection<Statement> result = new LinkedList<>();
        for (Pipeline pipeline : pipelines.getPipelines().values()) {
            result.addAll(pipeline.getReferenceRdf());
        }
        return result;
    }

    public Collection<Statement> getReferenceAsRdf(Pipeline pipeline) {
        return pipeline.getReferenceRdf();
    }

    public Collection<Statement> getPipelineRdf(Pipeline pipeline)
            throws OperationFailed {
        File pipelineFile = pipeline.getFile();
        try {
            return RdfUtils.read(pipelineFile);
        } catch (Exception ex) {
            throw new OperationFailed("Can't read pipeline file.", ex);
        }
    }

    public Collection<Statement> getPipelineRdf(
            Pipeline pipeline,
            boolean includeTemplate,
            boolean includeMapping,
            boolean removePrivateConfig)
            throws BaseException {
        return pipelines.getPipelineRdf(
                pipeline, includeTemplate, includeMapping, removePrivateConfig);
    }

    /**
     * Create a new pipeline. The pipeline could be created from given RDF
     * or if none is given empty pipeline is created.
     *
     * <p>If pipeline is given in form of the RDF the migration, import and
     * update are performed on given pipeline.
     *
     * <p>If pipelineRdf is empty an "empty pipeline" is used instead.
     */
    public Pipeline createPipeline(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf)
            throws OperationFailed {
        return pipelines.createPipeline(pipelineRdf, optionsRdf);
    }

    public void updatePipeline(
            Pipeline pipeline, Collection<Statement> pipelineRdf)
            throws OperationFailed {
        pipelines.updatePipeline(pipeline, pipelineRdf);
    }

    public void deletePipeline(Pipeline pipeline) {
        pipelines.deletePipeline(pipeline);
    }

    /**
     * Perform pipeline modifications (migration, update) based on given
     * options and return modified pipeline. Operations are the similar as for
     * the {@link #createPipeline(Collection, Collection)} function.
     */
    public Collection<Statement> localizePipeline(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf) throws OperationFailed {
        return pipelines.localizePipeline(pipelineRdf, optionsRdf);
    }

}
