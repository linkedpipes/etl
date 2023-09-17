package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.library.pipeline.PipelineFactory;
import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import com.linkedpipes.etl.library.pipeline.adapter.PipelineToRdf;
import com.linkedpipes.etl.library.pipeline.adapter.RdfToRawPipeline;
import com.linkedpipes.etl.library.pipeline.migration.MigratePipeline;
import com.linkedpipes.etl.library.pipeline.migration.PipelineMigrationFailed;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.adapter.ReferenceTemplateToRdf;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.StorageService;
import com.linkedpipes.etl.storage.assistant.AssistantService;
import com.linkedpipes.etl.storage.distribution.ExportPipeline;
import com.linkedpipes.etl.storage.distribution.model.ExportPipelineOptions;
import com.linkedpipes.etl.storage.distribution.model.FullPipeline;
import com.linkedpipes.etl.storage.http.adapter.PipelineListToRdf;
import com.linkedpipes.etl.storage.http.adapter.RdfToCreatePipelineOptions;
import com.linkedpipes.etl.storage.http.model.CreatePipelineOptions;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

class PipelineServletService {

    private final PipelineFacade pipelineFacade;

    private final AssistantService assistantService;

    private final TemplateFacade templateFacade;

    public PipelineServletService(StorageService storageService) {
        this.pipelineFacade = storageService.getPipelineFacade();
        this.assistantService = storageService.getAssistantService();
        this.templateFacade = storageService.getTemplateFacade();
    }

    public void handleGetPipelineList(
            HttpServletRequest request, HttpServletResponse response) {
        Statements statements = PipelineListToRdf.asRdf(assistantService);
        ServletUtilities.sendResponse(request, response, statements);
    }

    /**
     * Return a pipeline identified with given resource.
     *
     * @param resource Resource of a pipeline to return.
     * @param includeTemplates If true also return all templates used by the pipeline.
     * @param removePrivateConfig If true remove private configuration.
     */
    public void handleGetPipeline(
            Resource resource,
            boolean includeTemplates, boolean removePrivateConfig,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        Pipeline pipeline;
        try {
            pipeline = pipelineFacade.getPipeline(resource);
        } catch (StorageException ex) {
            throw new ServerError("Can't get pipeline.", ex);
        }
        if (pipeline == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        Statements statements;
        if (includeTemplates || removePrivateConfig) {
            ExportPipeline exportPipeline = new ExportPipeline(templateFacade);
            ExportPipelineOptions options = new ExportPipelineOptions();
            options.includeTemplate = includeTemplates;
            options.removePrivateConfiguration = removePrivateConfig;
            FullPipeline fullPipeline;
            try {
                fullPipeline = exportPipeline.export(pipeline, options);
            } catch (StorageException ex) {
                throw new ServerError("Can't export pipeline.", ex);
            }
            statements = PipelineToRdf.asRdf(fullPipeline.pipeline());
            for (ReferenceTemplate template : fullPipeline.templates()) {
                statements.addAll(
                        ReferenceTemplateToRdf.definitionAsRdf(template));
                statements.addAll(
                        ReferenceTemplateToRdf.configurationAsRdf(template));
            }
        } else {
            statements = PipelineToRdf.asRdf(pipeline);
        }
        ServletUtilities.sendResponse(request, response, statements);
    }

    /**
     * Create a single new pipeline. When no pipeline definition is given,
     * a default definition is used.      *
     * Given pipeline definition is migrated to the latest pipeline version.
     *
     * As a response return the created pipeline.
     *
     * @param pipelineFile Optional data pipeline definition.
     * @param optionsFile Required pipeline configuration, must be exactly one.
     */
    public void handleCreatePipeline(
            MultipartFile pipelineFile,
            MultipartFile optionsFile,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        CreatePipelineOptions options = loadCreatePipelineOptions(optionsFile);
        Pipeline pipeline;
        // TODO: Split this into two functions.
        // We should have one function to create new pipeline from a template
        // and other to just store the pipeline.
        // As of now pipeline resource can be set only for
        // empty pipelines.
        if (pipelineFile == null) {
            pipeline = createEmptyPipeline(options);
        } else {
            pipeline = readAndMigratePipeline(pipelineFile);
        }
        try {
            pipelineFacade.storePipeline(pipeline);
        } catch (StorageException ex) {
            throw new ServerError("Can't store pipeline.", ex);
        }
        ServletUtilities.sendResponse(
                request, response,
                PipelineToRdf.asRdf(pipeline));
    }

    private CreatePipelineOptions loadCreatePipelineOptions(
            MultipartFile file) throws InvalidRequest {
        Statements statements = ServletUtilities.read(file);
        List<CreatePipelineOptions> candidates =
                RdfToCreatePipelineOptions.asCreatePipelineOptions(statements);
        if (candidates.size() != 1) {
            throw new InvalidRequest(
                    "Invalid number of pipeline options '{}'.",
                    candidates.size());
        }
        return candidates.get(0);
    }

    private Pipeline createEmptyPipeline(CreatePipelineOptions options) {
        Resource pipelineResource = options.targetResource;
        if (pipelineResource == null || pipelineResource.isBNode()) {
            pipelineResource = pipelineFacade.reservePipelineResource();
        }
        return PipelineFactory.createEmpty(
                pipelineResource, options.targetLabel);
    }

    private Pipeline readAndMigratePipeline(
            MultipartFile pipelineFile) throws InvalidRequest, ServerError {
        Statements statements = ServletUtilities.read(pipelineFile);
        List<RawPipeline> candidates =
                RdfToRawPipeline.asRawPipelines(statements);
        if (candidates.size() != 1) {
            throw new InvalidRequest(
                    "Only one pipeline expected found '{}'.",
                    candidates.size());
        }
        RawPipeline rawPipeline = candidates.get(0);
        if (!MigratePipeline.shouldMigrate(rawPipeline)) {
            return rawPipeline.toPipeline();
        }
        try {
            var templateToPlugin = templateFacade.getTemplateToPluginMap();
            return (new MigratePipeline(templateToPlugin)).migrate(rawPipeline);
        } catch (PipelineMigrationFailed ex) {
            throw new ServerError("Can't migrate pipeline.", ex);
        } catch (StorageException ex) {
            throw new ServerError("Can't get templates.", ex);
        }
    }

    /**
     * Store given pipeline under given IRI.
     *
     * Pipeline is migrated to the latest version.
     *
     * This function does not return any data.
     *
     * @param pipelineFile Pipeline definition.
     */
    public void handleUpdatePipeline(
            MultipartFile pipelineFile,
            HttpServletResponse response)
            throws InvalidRequest, ServerError {
        Pipeline pipeline = readAndMigratePipeline(pipelineFile);
        try {
            // Only update time of change to now.
            pipelineFacade.storePipeline(new Pipeline(
                    pipeline.resource(),
                    pipeline.created(),
                    LocalDateTime.now(),
                    pipeline.label(),
                    pipeline.version(),
                    pipeline.note(),
                    pipeline.tags(),
                    pipeline.executionProfile(),
                    pipeline.components(),
                    pipeline.dataFlows(),
                    pipeline.controlFlows()));
        } catch (StorageException ex) {
            throw new ServerError("Can't update pipeline.", ex);
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

    /**
     * Delete pipeline with given resource.
     */
    public void handleDeletePipeline(
            Resource resource,
            HttpServletResponse response)
            throws ServerError {
        try {
            pipelineFacade.deletePipeline(resource);
        } catch (StorageException ex) {
            throw new ServerError("Can't delete pipeline '{}'.", resource, ex);
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

}
