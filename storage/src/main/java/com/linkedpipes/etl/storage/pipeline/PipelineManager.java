package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.pipeline.info.InfoFacade;
import com.linkedpipes.etl.storage.pipeline.transformation.TransformationFacade;
import com.linkedpipes.etl.storage.pipeline.transformation.TransformationFailed;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.template.mapping.MappingFacade;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.linkedpipes.etl.storage.rdf.RdfUtils.write;

/**
 * Manage pipeline storage.
 */
@Service
class PipelineManager {

    private static final Logger LOG =
            LoggerFactory.getLogger(PipelineManager.class);

    private static final SimpleDateFormat DATE_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private Configuration configuration;

    @Autowired
    private InfoFacade infoFacade;

    @Autowired
    private MappingFacade mappingFacade;

    @Autowired
    private TemplateFacade templatesFacade;

    @Autowired
    private TransformationFacade transformationFacade;

    /**
     * Store pipelines.
     */
    private final Map<String, Pipeline> pipelines = new HashMap<>();

    /**
     * Contains list of used or reserved IRIs.
     */
    private final Set<String> reserved = new HashSet<>();

    /**
     * Object use as a lock, for inner synchronisation.
     */
    private final Object lock = new Object();

    @PostConstruct
    public void initialize() {
        final File pipelineDirectory = configuration.getPipelinesDirectory();
        if (!pipelineDirectory.exists()) {
            pipelineDirectory.mkdirs();
        }
        for (File file : pipelineDirectory.listFiles()) {
            // Read only files without the .backup extension.
            final String fileName = file.getName().toLowerCase();
            if (!file.isFile() || fileName.endsWith(".backup")) {
                continue;
            }
            // Load pipeline.
            try {
                loadPipeline(file);
            } catch (Exception ex) {
                LOG.error("Ignoring invalid pipeline: {}", file, ex);
            }
        }
    }

    /**
     * Load pipeline from a given file. Check version, perform migration
     * (if necessary) and add pipeline to pipelines.
     *
     * @param file
     * @return Loaded pipeline.
     */
    protected void loadPipeline(File file)
            throws PipelineFacade.OperationFailed {
        Collection<Statement> pipelineRdf;
        PipelineInfo info;
        try {
            pipelineRdf = RdfUtils.read(file);
            info = new PipelineInfo();
            PojoLoader.loadOfType(pipelineRdf, Pipeline.TYPE, info);
        } catch (PojoLoader.CantLoadException | RdfUtils.RdfException ex) {
            throw new PipelineFacade.OperationFailed("Can't createMappingFromStatements pipeline: {}",
                    file, ex);
        }
        if (info.getVersion() != Pipeline.VERSION_NUMBER) {
            backupPipeline(file, pipelineRdf);
            // pipeline.
            try {
                pipelineRdf = transformationFacade
                        .migrateThrowOnWarning(pipelineRdf);
                info = new PipelineInfo();
                PojoLoader.loadOfType(pipelineRdf, Pipeline.TYPE, info);
            } catch (TransformationFailed | PojoLoader.CantLoadException ex) {
                throw new PipelineFacade.OperationFailed(
                        "Can't pipeline pipeline: {}", file, ex);
            }
            updatePipelineFile(file, pipelineRdf);
        }
        // Create pipeline record.
        Pipeline pipeline = new Pipeline(file, info);
        createPipelineReference(pipeline);
        infoFacade.onPipelineCreate(pipeline, pipelineRdf);
        //
        pipelines.put(pipeline.getIri(), pipeline);
        reserved.add(pipeline.getIri());
    }

    private void backupPipeline(File file, Collection<Statement> pipelineRdf)
            throws PipelineFacade.OperationFailed {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        final File backupFile = new File(file.getParent(), fileName +
                "_" + DATE_FORMAT.format(new Date()) + ".trig.backup");
        try {
            RdfUtils.write(backupFile, RDFFormat.TRIG, pipelineRdf);
        } catch (RdfUtils.RdfException ex) {
            throw new PipelineFacade.OperationFailed(
                    "Can't exportForTemplates backup file: {}", backupFile, ex);
        }
    }

    private void updatePipelineFile(
            File file,
            Collection<Statement> pipelineRdf)
            throws PipelineFacade.OperationFailed {
        try {
            RdfUtils.write(file, RDFFormat.TRIG, pipelineRdf);
        } catch (RdfUtils.RdfException ex) {
            throw new PipelineFacade.OperationFailed(
                    "Can't exportForTemplates updated pipeline to file: {}", file, ex);
        }
    }

    /**
     * Create and set reference for the pipeline. The pipeline reference
     * consists from typed pipeline resource and labels. Use information
     * in the pipeline.info.
     *
     * @param pipeline
     */
    protected static void createPipelineReference(Pipeline pipeline) {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final IRI pipelineIri = vf.createIRI(pipeline.getIri());
        final List<Statement> referenceRdf = new ArrayList<>(4);
        //
        referenceRdf.add(vf.createStatement(pipelineIri,
                RDF.TYPE, Pipeline.TYPE, pipelineIri));
        for (Value label : pipeline.getInfo().getLabels()) {
            referenceRdf.add(vf.createStatement(pipelineIri, SKOS.PREF_LABEL,
                    label, pipelineIri));
        }
        final IRI tagIri = vf.createIRI(
                "http://etl.linkedpipes.com/ontology/tag");
        for (Value tag : pipeline.getInfo().getTags()) {
            referenceRdf.add(vf.createStatement(pipelineIri, tagIri,
                    tag, pipelineIri));
        }
        //
        pipeline.setReferenceRdf(referenceRdf);
    }

    /**
     * @return A map of all pipelines.
     */
    public Map<String, Pipeline> getPipelines() {
        return Collections.unmodifiableMap(pipelines);
    }

    /**
     * @return Reserved pipeline IRI as string.
     */
    public String reservePipelineIri() {
        String iri;
        synchronized (lock) {
            do {
                iri = configuration.getDomainName() +
                        "/resources/pipelines/created-" +
                        (new Date()).getTime();
            } while (reserved.contains(iri));
            reserved.add(iri);
        }
        return iri;
    }

    /**
     * @param iri
     * @return Empty pipeline of given IRI.
     */
    private final Collection<Statement> createEmptyPipeline(IRI iri) {
        final ValueFactory vf = SimpleValueFactory.getInstance();

        IRI profileIri = vf.createIRI(iri.stringValue() +
                "/profile/default");

        return Arrays.asList(
                vf.createStatement(iri, RDF.TYPE, Pipeline.TYPE, iri),
                vf.createStatement(iri, Pipeline.HAS_VERSION,
                        vf.createLiteral(Pipeline.VERSION_NUMBER), iri),
                vf.createStatement(iri, SKOS.PREF_LABEL,
                        vf.createLiteral(iri.stringValue()), iri),
                vf.createStatement(iri,
                        vf.createIRI(LP_PIPELINE.HAS_PROFILE)
                        , profileIri, iri),
                vf.createStatement(profileIri, RDF.TYPE,
                        vf.createIRI(LP_PIPELINE.PROFILE), iri),
                vf.createStatement(profileIri,
                        vf.createIRI(LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY),
                        vf.createIRI(LP_PIPELINE.SINGLE_REPOSITORY), iri),
                vf.createStatement(profileIri,
                        vf.createIRI(LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE),
                        vf.createIRI(LP_PIPELINE.NATIVE_STORE), iri)
        );
    }

    /**
     * Import pipeline from given data and apply given options. If the
     * pipelineRdf is empty then importJarComponent new empty pipeline.
     *
     * @param pipelineRdf
     * @param optionsRdf
     * @return
     */
    public Pipeline createPipeline(Collection<Statement> pipelineRdf,
                                   Collection<Statement> optionsRdf)
            throws PipelineFacade.OperationFailed {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        // Prepare pipeline IRI and update pipeline resources.
        final IRI iri = valueFactory.createIRI(reservePipelineIri());
        if (pipelineRdf.isEmpty()) {
            pipelineRdf = createEmptyPipeline(iri);
        } else {
            pipelineRdf = localizePipeline(pipelineRdf, optionsRdf, iri);
        }
        // Read pipeline info.
        PipelineInfo info = new PipelineInfo();
        try {
            PojoLoader.loadOfType(pipelineRdf, Pipeline.TYPE, info);
        } catch (PojoLoader.CantLoadException ex) {
            throw new PipelineFacade.OperationFailed(
                    "Can't createMappingFromStatements pipeline after localization.", ex);
        }
        // Add to pipeline list.
        final String fileName = iri.getLocalName() + ".trig";
        final Pipeline pipeline = new Pipeline(new File(
                configuration.getPipelinesDirectory(), fileName),
                info);
        pipeline.setInfo(info);
        createPipelineReference(pipeline);
        // Save to dist.
        try {
            RdfUtils.write(pipeline.getFile(), RDFFormat.TRIG, pipelineRdf);
        } catch (RdfUtils.RdfException ex) {
            pipeline.getFile().delete();
            //
            throw new PipelineFacade.OperationFailed(
                    "Can't exportForTemplates pipeline to {}", pipeline.getFile(), ex);
        }
        //
        pipelines.put(iri.stringValue(), pipeline);
        infoFacade.onPipelineCreate(pipeline, pipelineRdf);
        return pipeline;
    }

    /**
     * Update pipeline definition and perform all related operations.
     *
     * @param pipeline
     * @param pipelineRdf
     */
    public void updatePipeline(Pipeline pipeline,
                               Collection<Statement> pipelineRdf)
            throws PipelineFacade.OperationFailed {
        // Update pipeline in-memory model.
        PipelineInfo info = new PipelineInfo();
        try {
            PojoLoader.loadOfType(pipelineRdf, Pipeline.TYPE, info);
            pipeline.setInfo(info);
            //
            createPipelineReference(pipeline);
        } catch (PojoLoader.CantLoadException ex) {
            throw new PipelineFacade.OperationFailed(
                    "Can't createMappingFromStatements pipeline.", ex);
        }
        // Write to disk.
        try {
            write(pipeline.getFile(), RDFFormat.TRIG, pipelineRdf);
        } catch (RdfUtils.RdfException ex) {
            throw new PipelineFacade.OperationFailed(
                    "Can't exportForTemplates pipeline: {}", pipeline.getFile(), ex);
        }
        infoFacade.onPipelineUpdate(pipeline, pipelineRdf);
        // TODO Use events to notify all about pipeline change !
    }

    /**
     * Delete given pipeline.
     *
     * @param pipeline
     */
    public void deletePipeline(Pipeline pipeline) {
        // TODO Add tomb-stone
        infoFacade.onPipelineDelete(pipeline);
        pipeline.getFile().delete();
        pipelines.remove(pipeline.getIri());
        // TODO Use event to notify about changes !
    }

    /**
     * Return RDF definition of the pipeline with optional additional
     * information.
     *
     * @param pipeline
     * @param includeTemplate
     * @param includeMapping
     * @return
     */
    public Collection<Statement> getPipelineRdf(Pipeline pipeline,
                                                boolean includeTemplate, boolean includeMapping)
            throws BaseException {
        // Read pipeline.
        final Collection<Statement> pipelineRdf;
        try {
            pipelineRdf = RdfUtils.read(pipeline.getFile());
        } catch (Exception ex) {
            throw new PipelineFacade.OperationFailed("Can't createMappingFromStatements file.", ex);
        }
        // Add additional data.
        Set<Template> templates = null;
        final Collection<Statement> additionalRdf = new LinkedList<>();
        if (includeTemplate) {
            if (templates == null) {
                templates = getTemplates(pipelineRdf, pipeline.getIri());
            }
            //
            for (Template template : templates) {
                // We need to remove duplicity from definition and interface.
                final Set<Statement> templateRdf = new HashSet<>();
                templateRdf.addAll(templatesFacade.getInterface(template));
                templateRdf.addAll(templatesFacade.getDefinition(template));
                //
                additionalRdf.addAll(templateRdf);
                additionalRdf.addAll(templatesFacade
                        .getConfig(template));
            }
        }
        if (includeMapping) {
            if (templates == null) {
                templates = getTemplates(pipelineRdf, pipeline.getIri());
            }
            //
            additionalRdf.addAll(mappingFacade.exportForTemplates(templates));
        }
        // Merge and return.
        pipelineRdf.addAll(additionalRdf);
        return pipelineRdf;
    }

    /**
     * Return list of all templates used in the pipeline, also include
     * transitive templates.
     *
     * @param pipelineRdf
     * @return
     */
    private Set<Template> getTemplates(Collection<Statement> pipelineRdf,
                                       String pipelineIri) {
        final Set<Template> templates = new HashSet<>();
        for (Statement statement : pipelineRdf) {
            if (statement.getPredicate().stringValue().equals(
                    "http://linkedpipes.com/ontology/template") &&
                    statement.getContext().stringValue().equals(pipelineIri)) {
                Template template = templatesFacade.getTemplate(
                        statement.getObject().stringValue());
                templates.addAll(templatesFacade.getAncestorsWithoutJarTemplate(
                        template));
            }
        }
        return templates;
    }

    /**
     * Prepare given pipeline to be used on this instance.
     *
     * @param pipelineRdf
     * @param optionsRdf
     * @param pipelineIri
     * @return
     */
    private Collection<Statement> localizePipeline(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf,
            IRI pipelineIri)
            throws PipelineFacade.OperationFailed {
        try {
            pipelineRdf = transformationFacade.localizeAndMigrate(
                    pipelineRdf, optionsRdf, pipelineIri);
        } catch (TransformationFailed ex) {
            throw new PipelineFacade.OperationFailed(
                    "Can't transform pipeline.", ex);
        }
        return pipelineRdf;
    }

    public Collection<Statement> localizePipeline(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf)
            throws PipelineFacade.OperationFailed {
        try {
            pipelineRdf = transformationFacade.localizeAndMigrate(
                    pipelineRdf, optionsRdf, null);
        } catch (TransformationFailed ex) {
            throw new PipelineFacade.OperationFailed(
                    "Can't transform pipeline.", ex);
        }
        return pipelineRdf;
    }

}
