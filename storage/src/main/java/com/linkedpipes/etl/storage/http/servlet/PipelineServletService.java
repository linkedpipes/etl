package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.library.pipeline.PipelineFactory;
import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import com.linkedpipes.etl.library.pipeline.adapter.PipelineToRdf;
import com.linkedpipes.etl.library.pipeline.adapter.RdfToRawPipeline;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.adapter.ReferenceTemplateToRdf;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.StorageService;
import com.linkedpipes.etl.storage.assistant.AssistantService;
import com.linkedpipes.etl.storage.distribution.ExportPipeline;
import com.linkedpipes.etl.storage.distribution.adapter.RdfToImportPipelineOptions;
import com.linkedpipes.etl.storage.distribution.model.ExportPipelineOptions;
import com.linkedpipes.etl.storage.distribution.model.FullPipeline;
import com.linkedpipes.etl.storage.distribution.model.ImportPipelineOptions;
import com.linkedpipes.etl.storage.http.adapter.PipelineListToRdf;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

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
            statements = PipelineToRdf.asRdf(pipeline);
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

    public void handleCreatePipeline(
            MultipartFile pipelineFile,
            MultipartFile optionsFile,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        // Load user provided data.
        List<ImportPipelineOptions> candidateOptions =
                loadImportPipelineOptions(optionsFile);
        if (candidateOptions.size() != 1) {
            throw new InvalidRequest(
                    "Invalid number of pipeline options '{}'.",
                    candidateOptions.size());
        }
        ImportPipelineOptions options = candidateOptions.get(0);
        Resource pipelineResource = options.targetResource;
        if (pipelineResource == null || pipelineResource.isBNode()) {
            pipelineResource = pipelineFacade.reservePipelineResource();
        }
        Pipeline pipeline;
        if (pipelineFile == null) {
            pipeline = PipelineFactory.createEmpty(
                    pipelineResource, options.targetLabel);
        } else {
            pipeline = readPipeline(pipelineFile);
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

    private List<ImportPipelineOptions> loadImportPipelineOptions(
            MultipartFile file) throws InvalidRequest {
        Statements statements = ServletUtilities.read(file);
        return RdfToImportPipelineOptions.asImportPipelineOptions(statements);
    }

    private Pipeline readPipeline(MultipartFile pipelineFile)
            throws InvalidRequest {
        Statements statements = ServletUtilities.read(pipelineFile);
        List<RawPipeline> candidates =
                RdfToRawPipeline.asRawPipelines(statements);
        if (candidates.size() != 1) {
            throw new InvalidRequest(
                    "Only one pipeline expected found '{}'.",
                    candidates.size());
        }
        return candidates.get(0).toPipeline();
    }

    public void handleUpdatePipeline(
            MultipartFile pipelineFile,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        Pipeline pipeline = readPipeline(pipelineFile);
        try {
            pipelineFacade.storePipeline(new Pipeline(
                    pipeline.resource(), pipeline.created(),
                    LocalDateTime.now(),
                    pipeline.label(), pipeline.version(), pipeline.note(),
                    pipeline.tags(), pipeline.executionProfile(),
                    pipeline.components(), pipeline.dataFlows(),
                    pipeline.controlFlows()));
        } catch (StorageException ex) {
            throw new ServerError("Can't update pipeline.", ex);
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

    public void handleDeletePipeline(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        try {
            pipelineFacade.deletePipeline(resource);
        } catch (StorageException ex) {
            throw new ServerError("Can't delete pipeline '{}'.", resource, ex);
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

}
