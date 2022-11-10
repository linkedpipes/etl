package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.ReferenceTemplateFacade;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PipelineService {

    private static final Logger LOG =
            LoggerFactory.getLogger(PipelineService.class);

    private final PipelineEvents pipelineEvents;

    private final PipelineRepository repository;

    // TODO We should be able to get rid of this and just require an interface.
    private final ReferenceTemplateFacade referenceFacade;

    public PipelineService(
            PipelineEvents pipelineEvents,
            PipelineRepository repository,
            ReferenceTemplateFacade referenceFacade) {
        this.pipelineEvents = pipelineEvents;
        this.repository = repository;
        this.referenceFacade = referenceFacade;
    }

    /**
     * Iterate repository and notify all listeners to loaded
     * pipelines.
     */
    public void initialize() throws StorageException {
        LOG.debug("Initializing pipeline service ... ");
        int pipelineCounter = 0;
        for (Resource resource : repository.listPipelines()) {
            Pipeline pipeline;
            try {
                pipeline = repository.loadPipeline(resource);
            } catch (StorageException ex) {
                LOG.warn("Can't load pipeline '{}'.", resource, ex);
                continue;
            }
            pipelineEvents.onPipelineLoaded(pipeline);
            ++pipelineCounter;
        }
        LOG.info("Initializing pipeline service ... done with {} pipelines",
                pipelineCounter);
    }

    /**
     * Create new pipeline with given resource.
     */
    public void createPipeline(Pipeline pipeline, Resource resource)
            throws StorageException {
        pipeline = (new ChangePipelineResource(
                iri -> iri, referenceFacade::findPluginTemplate
        )).localize(pipeline, resource);
        repository.storePipeline(pipeline);
        pipelineEvents.onPipelineCreated(pipeline);
    }

    /**
     * Store existing pipeline or create new one with the resource
     * stored in the pipeline.
     */
    public void storePipeline(Pipeline pipeline) throws StorageException {
        if (pipeline.resource() instanceof BNode) {
            throw new StorageException(
                    "Pipeline resource can not be a blank node.");
        }
        Pipeline previous = repository.loadPipeline(pipeline.resource());
        if (previous == null) {
            createPipeline(pipeline, pipeline.resource());
        } else {
            updatePipeline(previous, pipeline);
        }
    }

    private void updatePipeline(Pipeline previous, Pipeline next)
            throws StorageException {
        repository.storePipeline(next);
        pipelineEvents.onPipelineUpdated(previous, next);
    }

    public void deletePipeline(Resource resource) throws StorageException {
        Pipeline pipeline = repository.loadPipeline(resource);
        if (pipeline == null) {
            return;
        }
        repository.deletePipeline(resource);
        pipelineEvents.onPipelineDeleted(pipeline);
    }

    /**
     * Force reload.
     */
    public void reloadPipelines() throws StorageException {
        pipelineEvents.onPipelineReload();
        repository.reload();
        initialize();
    }

//    private final AssistantService infoFacade;
//
//    private final ExportPipeline exportPipeline;
//
//    /**
//     * Contains list of used or reserved IRIs.
//     */
//    private final Set<String> reserved = new HashSet<>();
//
//    /**
//     * Object use as a lock, for inner synchronisation.
//     */
//    private final Object lock = new Object();
//
//    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
//
//    public PipelineService(
//            Configuration configuration, AssistantService info,
//            TransformationFacade transformation, PipelineRepository storage,
//            ExportPipeline exportPipeline) {
//        this.configuration = configuration;
//        this.infoFacade = info;
//        this.transformationFacade = transformation;
//        this.storage = storage;
//        this.exportPipeline = exportPipeline;
//    }
//
//    @PostConstruct
//    public void initialize() {
//        storage.loadPipelines((pipeline, rdf) -> {
//            infoFacade.onPipelineCreate(pipeline, rdf);
//            reserved.add(pipeline.getIri());
//        });
//    }
//
//    public Map<String, PipelineRef> getPipelines() {
//        return storage.getPipelines();
//    }
//
//    /**
//     * Import pipeline from given statements, if no data are given create
//     * an empty pipeline.
//     */
//    public PipelineRef createPipeline(
//            Collection<Statement> pipelineRdf, Collection<Statement> optionsRdf)
//            throws PipelineOperationFailed {
//        IRI reservedIri = reservePipelineIri();
//        if (pipelineRdf.isEmpty()) {
//            pipelineRdf = PipelineFactory.createEmpty(reservedIri);
//        } else {
//            pipelineRdf = localizePipeline(
//                    pipelineRdf, optionsRdf, reservedIri);
//        }
//        PipelineRef pipeline = storage.createPipeline(
//                selectPipelineIRI(pipelineRdf), pipelineRdf);
//        infoFacade.onPipelineCreate(pipeline, pipelineRdf);
//        reserved.remove(reservedIri.stringValue());
//        return pipeline;
//    }
//
//    private IRI reservePipelineIri() {
//        String iri;
//        synchronized (lock) {
//            do {
//                iri = configuration.getDomainName()
//                        + "/resources/pipelines/"
//                        + (new Date()).getTime();
//            } while (reserved.contains(iri));
//            reserved.add(iri);
//        }
//        return valueFactory.createIRI(iri);
//    }
//
//    private IRI selectPipelineIRI(Collection<Statement> pipeline)
//            throws PipelineOperationFailed {
//        PipelineInfo info = new PipelineInfo();
//        try {
//            PojoLoader.loadOfType(pipeline, PipelineRef.TYPE, info);
//        } catch (PojoLoader.CantLoadException ex) {
//            throw new PipelineOperationFailed(
//                    "Can't load pipeline info.", ex);
//        }
//        return valueFactory.createIRI(info.getIri());
//    }
//
//    public void updatePipeline(
//            PipelineRef pipeline, Collection<Statement> rdf)
//            throws PipelineOperationFailed {
//        infoFacade.onPipelineUpdate(pipeline, rdf);
//        storage.update(pipeline, rdf);
//    }
//
//    public void deletePipeline(PipelineRef pipeline) {
//        infoFacade.onPipelineDelete(pipeline);
//        storage.delete(pipeline);
//    }
//
//    /**
//     * Return RDF definition of the pipeline with optional additional
//     * information.
//     */
//    public Collection<Statement> getPipelineRdf(
//            PipelineRef pipeline,
//            boolean includeTemplate,
//            boolean includeMapping,
//            boolean removePrivateConfig)
//            throws StorageException {
//        Collection<Statement> rdf = storage.getPipelineRdf(pipeline);
//        if (!includeTemplate && !includeMapping && !removePrivateConfig) {
//            return rdf;
//        }
//        Collection<Statement> additionalRdf = new LinkedList<>();
//        Set<Template> templates = null;
//        if (includeTemplate) {
//            templates = exportPipeline.getTemplates(pipeline, rdf);
//            additionalRdf.addAll(exportPipeline.getTemplateRdf(templates));
//        }
//        if (includeMapping) {
//            if (templates == null) {
//                templates = exportPipeline.getTemplates(pipeline, rdf);
//            }
//            additionalRdf.addAll(exportPipeline.getMappingRdf(templates));
//        }
//        rdf.addAll(additionalRdf);
//        if (removePrivateConfig) {
//            exportPipeline.removePrivateConfiguration(rdf);
//        }
//        return rdf;
//    }
//
//    private Collection<Statement> localizePipeline(
//            Collection<Statement> pipeline, Collection<Statement> options,
//            IRI pipelineIri)
//            throws PipelineOperationFailed {
//        try {
//            pipeline = transformationFacade.localizeAndMigrate(
//                    pipeline, options, pipelineIri);
//        } catch (TransformationFailed ex) {
//            throw new PipelineOperationFailed(
//                    "Can't transform pipeline.", ex);
//        }
//        return pipeline;
//    }
//
//    public Collection<Statement> localizePipeline(
//            Collection<Statement> pipeline, Collection<Statement> options)
//            throws PipelineOperationFailed {
//        try {
//            pipeline = transformationFacade.localizeAndMigrate(
//                    pipeline, options, null);
//        } catch (TransformationFailed ex) {
//            throw new PipelineOperationFailed(
//                    "Can't transform pipeline.", ex);
//        }
//        return pipeline;
//    }
//
//    public void reload() {
//        Set<String> existingPipelines = storage.getPipelines().keySet();
//        storage.loadPipelines((pipeline, rdf) -> {
//            if (existingPipelines.contains(pipeline.getIri())) {
//                // Existing pipeline.
//                infoFacade.onPipelineUpdate(pipeline, rdf);
//            } else {
//                // New pipeline.
//                infoFacade.onPipelineCreate(pipeline, rdf);
//                reserved.add(pipeline.getIri());
//            }
//        });
//    }

}
