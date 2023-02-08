package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.ConfigurationHolder;
import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;

import java.util.Set;

public class PipelineFacade {

    private final ConfigurationHolder configuration;

    private final PipelineService service;

    private final PipelineRepository repository;

    public PipelineFacade(
            ConfigurationHolder configuration,
            PipelineService service,
            PipelineRepository repository) {
        this.configuration = configuration;
        this.service = service;
        this.repository = repository;
    }

    /**
     * Reserve and return new reference template resource.
     */
    public Resource reservePipelineResource() {
        return repository.reserveResource(
                Pipeline::createResource, configuration.getDomainName());
    }

    /**
     * Return local pipeline resource with given suffix. This may
     * be resource of existing pipeline.
     */
    public Resource createPipelineResourceWithSuffix(String suffix) {
        return Pipeline.createResource(configuration.getDomainName(), suffix);
    }

//    public PipelineRef getPipeline(String iri) {
//        return service.getPipelines().get(iri);
//    }

    public Pipeline getPipeline(Resource resource) throws StorageException {
        return repository.loadPipeline(resource);
    }

//    public Collection<PipelineRef> getService() {
//        return service.getPipelines().values();
//    }


    public Set<Resource> getPipelines() throws StorageException {
        return repository.listPipelines();
    }

    /**
     * Create new pipeline with given source, or use new one.
     * Return resource of given pipeline.
     */
    public Resource createPipeline(Pipeline pipeline, Resource resource)
            throws StorageException {
        if (resource == null || resource instanceof BNode) {
            resource = reservePipelineResource();
        }
        service.createPipeline(pipeline, resource);
        return resource;
    }

    public void storePipeline(Pipeline pipeline) throws StorageException {
        service.storePipeline(pipeline);
    }

    public void deletePipeline(Resource resource) throws StorageException {
        service.deletePipeline(resource);
    }

    /**
     * Force storage reload.
     */
    public void reloadPipelines() throws StorageException {
        service.reloadPipelines();
    }

//    public Collection<Statement> getReferenceAsRdf() {
//        Collection<Statement> result = new LinkedList<>();
//        for (PipelineRef pipeline : service.getPipelines().values()) {
//            result.addAll(pipeline.getReferenceRdf());
//        }
//        return result;
//    }
//
//    public Collection<Statement> getReferenceAsRdf(PipelineRef pipeline) {
//        return pipeline.getReferenceRdf();
//    }
//
//    public Collection<Statement> getPipelineRdf(PipelineRef pipeline)
//            throws PipelineOperationFailed {
//        File pipelineFile = pipeline.getFile();
//        try {
//            return RdfUtils.read(pipelineFile);
//        } catch (Exception ex) {
//            throw new PipelineOperationFailed("Can't read pipeline file.", ex);
//        }
//    }
//
//    public Collection<Statement> getPipelineRdf(
//            PipelineRef pipeline,
//            boolean includeTemplate,
//            boolean includeMapping,
//            boolean removePrivateConfig)
//            throws StorageException {
//        return service.getPipelineRdf(
//                pipeline, includeTemplate, includeMapping, removePrivateConfig);
//    }

//    /**
//     * Create a new pipeline. The pipeline could be created from given RDF
//     * or if none is given empty pipeline is created.
//     *
//     * <p>If pipeline is given in form of the RDF the migration, import and
//     * update are performed on given pipeline.
//     *
//     * <p>If pipelineRdf is empty an "empty pipeline" is used instead.
//     */
//    public PipelineRef createPipeline(
//            Collection<Statement> pipelineRdf,
//            Collection<Statement> optionsRdf)
//            throws PipelineOperationFailed {
//        return service.createPipeline(pipelineRdf, optionsRdf);
//    }

//    /**
//     * Perform pipeline modifications (migration, update) based on given
//     * options and return modified pipeline. Operations are the similar as for
//     * the {@link #createPipeline(Collection, Collection)} function.
//     */
//    public Collection<Statement> localizePipeline(
//            Collection<Statement> pipelineRdf,
//            Collection<Statement> optionsRdf) throws PipelineOperationFailed {
//        return service.localizePipeline(pipelineRdf, optionsRdf);
//    }

}
