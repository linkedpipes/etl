package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

@Service
public class PipelineFacade {

    private final PipelineService service;

    @Autowired
    public PipelineFacade(PipelineService service) {
        this.service = service;
    }

    public PipelineRef getPipeline(String iri) {
        return service.getPipelines().get(iri);
    }

    public Collection<PipelineRef> getService() {
        return service.getPipelines().values();
    }

    public Collection<Statement> getReferenceAsRdf() {
        Collection<Statement> result = new LinkedList<>();
        for (PipelineRef pipeline : service.getPipelines().values()) {
            result.addAll(pipeline.getReferenceRdf());
        }
        return result;
    }

    public Collection<Statement> getReferenceAsRdf(PipelineRef pipeline) {
        return pipeline.getReferenceRdf();
    }

    public Collection<Statement> getPipelineRdf(PipelineRef pipeline)
            throws OperationFailed {
        File pipelineFile = pipeline.getFile();
        try {
            return RdfUtils.read(pipelineFile);
        } catch (Exception ex) {
            throw new OperationFailed("Can't read pipeline file.", ex);
        }
    }

    public Collection<Statement> getPipelineRdf(
            PipelineRef pipeline,
            boolean includeTemplate,
            boolean includeMapping,
            boolean removePrivateConfig)
            throws StorageException {
        return service.getPipelineRdf(
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
    public PipelineRef createPipeline(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf)
            throws OperationFailed {
        return service.createPipeline(pipelineRdf, optionsRdf);
    }

    public void updatePipeline(
            PipelineRef pipeline, Collection<Statement> pipelineRdf)
            throws OperationFailed {
        service.updatePipeline(pipeline, pipelineRdf);
    }

    public void deletePipeline(PipelineRef pipeline) {
        service.deletePipeline(pipeline);
    }

    /**
     * Perform pipeline modifications (migration, update) based on given
     * options and return modified pipeline. Operations are the similar as for
     * the {@link #createPipeline(Collection, Collection)} function.
     */
    public Collection<Statement> localizePipeline(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf) throws OperationFailed {
        return service.localizePipeline(pipelineRdf, optionsRdf);
    }

}
