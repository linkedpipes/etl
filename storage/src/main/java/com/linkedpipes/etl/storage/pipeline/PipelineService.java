package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.pipeline.info.InfoFacade;
import com.linkedpipes.etl.storage.pipeline.transformation.TransformationFacade;
import com.linkedpipes.etl.storage.pipeline.transformation.TransformationFailed;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.template.Template;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@Service
class PipelineService {

    private final Configuration configuration;

    private final InfoFacade infoFacade;

    private final TransformationFacade transformationFacade;

    private final PipelineRepository storage;

    private final ExportPipeline exportPipeline;

    /**
     * Contains list of used or reserved IRIs.
     */
    private final Set<String> reserved = new HashSet<>();

    /**
     * Object use as a lock, for inner synchronisation.
     */
    private final Object lock = new Object();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public PipelineService(
            Configuration configuration, InfoFacade info,
            TransformationFacade transformation, PipelineRepository storage,
            ExportPipeline exportPipeline) {
        this.configuration = configuration;
        this.infoFacade = info;
        this.transformationFacade = transformation;
        this.storage = storage;
        this.exportPipeline = exportPipeline;
    }

    @PostConstruct
    public void initialize() {
        storage.loadPipelines((pipeline, rdf) -> {
            infoFacade.onPipelineCreate(pipeline, rdf);
            reserved.add(pipeline.getIri());
        });
    }

    public Map<String, PipelineRef> getPipelines() {
        return storage.getPipelines();
    }

    /**
     * Import pipeline from given statements, if no data are given create
     * an empty pipeline.
     */
    public PipelineRef createPipeline(
            Collection<Statement> pipelineRdf, Collection<Statement> optionsRdf)
            throws OperationFailed {
        IRI reservedIri = reservePipelineIri();
        if (pipelineRdf.isEmpty()) {
            pipelineRdf = EmptyPipelineFactory.create(reservedIri);
        } else {
            pipelineRdf = localizePipeline(
                    pipelineRdf, optionsRdf, reservedIri);
        }
        PipelineRef pipeline = storage.createPipeline(
                selectPipelineIRI(pipelineRdf), pipelineRdf);
        infoFacade.onPipelineCreate(pipeline, pipelineRdf);
        reserved.remove(reservedIri.stringValue());
        return pipeline;
    }

    private IRI reservePipelineIri() {
        String iri;
        synchronized (lock) {
            do {
                iri = configuration.getDomainName()
                        + "/resources/pipelines/"
                        + (new Date()).getTime();
            } while (reserved.contains(iri));
            reserved.add(iri);
        }
        return valueFactory.createIRI(iri);
    }

    private IRI selectPipelineIRI(Collection<Statement> pipeline)
            throws OperationFailed {
        PipelineInfo info = new PipelineInfo();
        try {
            PojoLoader.loadOfType(pipeline, PipelineRef.TYPE, info);
        } catch (PojoLoader.CantLoadException ex) {
            throw new OperationFailed(
                    "Can't load pipeline info.", ex);
        }
        return valueFactory.createIRI(info.getIri());
    }

    public void updatePipeline(
            PipelineRef pipeline, Collection<Statement> rdf)
            throws OperationFailed {
        infoFacade.onPipelineUpdate(pipeline, rdf);
        storage.update(pipeline, rdf);
    }

    public void deletePipeline(PipelineRef pipeline) {
        infoFacade.onPipelineDelete(pipeline);
        storage.delete(pipeline);
    }

    /**
     * Return RDF definition of the pipeline with optional additional
     * information.
     */
    public Collection<Statement> getPipelineRdf(
            PipelineRef pipeline,
            boolean includeTemplate,
            boolean includeMapping,
            boolean removePrivateConfig)
            throws StorageException {
        Collection<Statement> rdf = storage.getPipelineRdf(pipeline);
        if (!includeTemplate && !includeMapping && !removePrivateConfig) {
            return rdf;
        }
        Collection<Statement> additionalRdf = new LinkedList<>();
        Set<Template> templates = null;
        if (includeTemplate) {
            templates = exportPipeline.getTemplates(pipeline, rdf);
            additionalRdf.addAll(exportPipeline.getTemplateRdf(templates));
        }
        if (includeMapping) {
            if (templates == null) {
                templates = exportPipeline.getTemplates(pipeline, rdf);
            }
            additionalRdf.addAll(exportPipeline.getMappingRdf(templates));
        }
        rdf.addAll(additionalRdf);
        if (removePrivateConfig) {
            exportPipeline.removePrivateConfiguration(rdf);
        }
        return rdf;
    }

    private Collection<Statement> localizePipeline(
            Collection<Statement> pipeline, Collection<Statement> options,
            IRI pipelineIri)
            throws OperationFailed {
        try {
            pipeline = transformationFacade.localizeAndMigrate(
                    pipeline, options, pipelineIri);
        } catch (TransformationFailed ex) {
            throw new OperationFailed(
                    "Can't transform pipeline.", ex);
        }
        return pipeline;
    }

    public Collection<Statement> localizePipeline(
            Collection<Statement> pipeline, Collection<Statement> options)
            throws OperationFailed {
        try {
            pipeline = transformationFacade.localizeAndMigrate(
                    pipeline, options, null);
        } catch (TransformationFailed ex) {
            throw new OperationFailed(
                    "Can't transform pipeline.", ex);
        }
        return pipeline;
    }

    public void reload() {
        Set<String> existingPipelines = storage.getPipelines().keySet();
        storage.loadPipelines((pipeline, rdf) -> {
            if (existingPipelines.contains(pipeline.getIri())) {
                // Existing pipeline.
                infoFacade.onPipelineUpdate(pipeline, rdf);
            } else {
                // New pipeline.
                infoFacade.onPipelineCreate(pipeline, rdf);
                reserved.add(pipeline.getIri());
            }
        });
    }

}
