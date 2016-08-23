package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.pipeline.importer.ImportFacade;
import com.linkedpipes.etl.storage.pipeline.migration.MigrationFacade;
import com.linkedpipes.etl.storage.pipeline.updater.UpdaterFacade;
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

        public OperationFailed(String message, Object... args) {
            super(message, args);
        }
    }

    @Autowired
    private PipelineManager pipelines;

    @Autowired
    private ImportFacade importFacade;

    @Autowired
    private MigrationFacade migrationFacade;

    @Autowired
    private UpdaterFacade updaterFacade;

    /**
     * @param iri
     * @return Pipeline of given IRI or null.
     */
    public Pipeline getPipeline(String iri) {
        return pipelines.getPipelines().get(iri);
    }

    /**
     * @return RDF references to all stored pipeline.
     */
    public Collection<Statement> getReferenceRdf() {
        final Collection<Statement> result = new LinkedList<>();
        for (Pipeline pipeline : pipelines.getPipelines().values()) {
            result.addAll(pipeline.getReferenceRdf());
        }
        return result;
    }

    /**
     * @param pipeline
     * @return An RDF reference for given pipeline..
     */
    public Collection<Statement> getReferenceRdf(Pipeline pipeline) {
        return pipeline.getReferenceRdf();
    }

    /**
     * @param pipeline
     * @return A full RDF definition of given pipeline.
     */
    public Collection<Statement> getPipelineRdf(Pipeline pipeline)
            throws OperationFailed {
        final File pipelineFile = pipeline.getFile();
        // TODO If we have the file in same format as required we could stream it.
        try {
            return RdfUtils.read(pipelineFile);
        } catch (Exception ex) {
            throw new OperationFailed("Can't read file.", ex);
        }
    }

    /**
     * Create a new pipeline. The pipeline could be created from given RDF
     * or if none is given empty pipeline is created.
     *
     * If pipeline is given in form of the RDF the migration, import and update
     * are performed on given pipeline.
     *
     * @param pipelineRdf If empty an "empty pipeline" is used instead.
     * @param optionsRdf
     * @return Created pipeline.
     */
    public Pipeline createPipeline(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf)
            throws BaseException {
        return pipelines.createPipeline(pipelineRdf, optionsRdf);
    }

    /**
     * Update pipeline from given RDF statements.
     *
     * @param pipeline
     * @param pipelineRdf
     */
    public void updatePipeline(Pipeline pipeline,
            Collection<Statement> pipelineRdf) throws OperationFailed {
        pipelines.updatePipeline(pipeline, pipelineRdf);
    }

    /**
     * Delete given pipeline.
     *
     * @param pipeline
     */
    public void deletePipeline(Pipeline pipeline) {
        pipelines.deletePipeline(pipeline);
    }

    public Collection<Statement> localizePipeline(
            Collection<Statement> pipelineRdf, Collection<Statement> optionsRdf)
            throws BaseException {
        return pipelines.localizePipeline(pipelineRdf, optionsRdf);
    }

}
