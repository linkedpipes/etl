package com.linkedpipes.etl.storage.http.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.linkedpipes.etl.library.pipeline.adapter.PipelineToRdf;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.ResourceToString;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.StorageService;
import com.linkedpipes.etl.storage.assistant.AssistantService;
import com.linkedpipes.etl.storage.distribution.ExportContent;
import com.linkedpipes.etl.storage.distribution.ImportPipeline;
import com.linkedpipes.etl.storage.distribution.ImportTemplate;
import com.linkedpipes.etl.storage.distribution.adapter.RdfToImportPipelineOptions;
import com.linkedpipes.etl.storage.distribution.adapter.RdfToImportTemplateOptions;
import com.linkedpipes.etl.storage.distribution.model.ImportPipelineOptions;
import com.linkedpipes.etl.storage.distribution.model.ImportTemplateOptions;
import com.linkedpipes.etl.storage.http.adapter.ImportResponseToRdf;
import com.linkedpipes.etl.storage.http.model.ImportResponse;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.unpacker.UnpackerFacade;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ManagementServletService {

    private static class IgnoreCloseStream extends InputStream {

        private final InputStream instance;

        public IgnoreCloseStream(InputStream instance) {
            this.instance = instance;
        }

        @Override
        public int read() throws IOException {
            return instance.read();
        }

        @Override
        public void close() {
            // No action.
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(ManagementServletService.class);

    private static final String EXPORT_FILTER_NONE = "NONE";

    private static final String EXPORT_FILTER_ALL = "ALL";

    public static final String EXPORT_TYPE_ZIP_WITH_LABELS = "ZIP_LABELS";

    public static final String EXPORT_TYPE_ZIP_WITH_IRI = "ZIP_IRI";

    public static final String EXPORT_TYPE_FILE = "FILE";

    private final PipelineFacade pipelineFacade;

    private final AssistantService assistantService;

    private final TemplateFacade templateFacade;

    private final UnpackerFacade unpackerFacade;

    private final ObjectMapper mapper = new ObjectMapper();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public ManagementServletService(StorageService storageService) {
        this.pipelineFacade = storageService.getPipelineFacade();
        this.assistantService = storageService.getAssistantService();
        this.templateFacade = storageService.getTemplateFacade();
        this.unpackerFacade = new UnpackerFacade(
                storageService.getConfiguration(),
                storageService.getTemplateFacade());
    }

    public void handleExport(
            String exportTemplatesFilter, String exportPipelinesFilter,
            boolean removePrivateConfig, String exportMode,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        Predicate<Resource> pipelineFilter =
                createFilter(exportPipelinesFilter);
        Predicate<Resource> templateFilter =
                createFilter(exportTemplatesFilter);
        ExportContent worker = new ExportContent(templateFacade);
        worker.setRemovePrivateConfiguration(removePrivateConfig);
        try {
            worker.addPipelines(collectPipelines(pipelineFilter));
            worker.addTemplates(collectTemplates(templateFilter));
        } catch (StorageException ex) {
            throw new ServerError("Can't collect data for export.", ex);
        }
        //
        switch (exportMode) {
            default:
            case EXPORT_TYPE_FILE:
                exportFile(worker, request, response);
                break;
            case EXPORT_TYPE_ZIP_WITH_IRI:
                exportZip(worker,
                        pipeline -> ResourceToString.asBase64Full(
                                pipeline.resource()),
                        template -> ResourceToString.asBase64Full(
                                template.resource()),
                        request, response);
                break;
            case EXPORT_TYPE_ZIP_WITH_LABELS:
                exportZip(worker,
                        Pipeline::label,
                        ReferenceTemplate::label,
                        request, response);
                break;
        }
    }

    private void exportFile(
            ExportContent worker,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        Statements statements;
        try {
            statements = worker.exportStatements();
        } catch (StorageException ex) {
            throw new ServerError("Can't create statements.", ex);
        }
        ServletUtilities.sendResponse(request, response, statements);
    }

    private void exportZip(
            ExportContent worker,
            Function<Pipeline, String> namePipeline,
            Function<ReferenceTemplate, String> nameTemplate,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        response.setHeader("Content-Type", "application/zip");
        try (OutputStream stream = response.getOutputStream()) {
            worker.exportZip(stream, namePipeline, nameTemplate);
        } catch (StorageException | IOException ex) {
            throw new ServerError("Can't create archive.", ex);
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

    private Predicate<Resource> createFilter(String filter)
            throws InvalidRequest {
        if (filter == null || EXPORT_FILTER_NONE.equals(filter.toUpperCase())) {
            return (iri) -> false;
        }
        if (EXPORT_FILTER_ALL.equals(filter.toUpperCase())) {
            return (iri) -> true;
        }
        return createFilterFromArray(filter);
    }

    private Predicate<Resource> createFilterFromArray(String value)
            throws InvalidRequest {
        JsonNode root;
        try {
            root = mapper.readTree(value);
        } catch (IOException ex) {
            throw new InvalidRequest("Can't parse query '{}'.", value, ex);
        }
        if (!(root instanceof ArrayNode)) {
            throw new InvalidRequest("Can't parse query '{}'.", value);
        }
        Set<Resource> result = new HashSet<>();
        for (JsonNode node : root) {
            result.add(valueFactory.createIRI(node.textValue()));
        }
        return result::contains;
    }

    private List<Pipeline> collectPipelines(Predicate<Resource> predicate)
            throws StorageException {
        List<Pipeline> result = new ArrayList<>();
        for (Resource resource : pipelineFacade.getPipelines()) {
            if (!predicate.test(resource)) {
                continue;
            }
            Pipeline pipeline = pipelineFacade.getPipeline(resource);
            result.add(pipeline);
        }
        return result;
    }

    private List<ReferenceTemplate> collectTemplates(
            Predicate<Resource> predicate) throws StorageException {
        List<ReferenceTemplate> result = new ArrayList<>();
        for (ReferenceTemplate template :
                templateFacade.getReferenceTemplates()) {
            if (!predicate.test(template.resource())) {
                continue;
            }
            result.add(template);
        }
        return result;
    }

    public void handleImport(
            MultipartFile contentFile, MultipartFile optionsFile,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        Statements statements;
        if (ServletUtilities.CONTENT_ZIP.equals(contentFile.getContentType())) {
            statements = loadZipContent(contentFile);
        } else {
            statements = loadRdfContent(contentFile);
        }
        // Load user provided data.
        List<ImportPipelineOptions> pipelineOptions
                = loadImportPipelineOptions(optionsFile);
        // Import template.
        ImportTemplate importTemplates =
                importTemplates(optionsFile, statements);
        // Import pipeline.
        ImportPipeline importPipelines = new ImportPipeline(
                templateFacade, templateFacade, pipelineFacade,
                importTemplates.getRemoteToLocal());
        try {
            importPipelines.loadFromStatements(statements.selector());
            importPipelines.importPipelines(pipelineOptions);
        } catch (StorageException ex) {
            throw new ServerError("Can't import pipelines.", ex);
        }
        Statements result = ImportResponseToRdf.asRdf(
                ImportResponse.create(importTemplates, importPipelines));
        ServletUtilities.sendResponse(request, response, result);
    }

    public Statements loadZipContent(MultipartFile contentFile)
            throws InvalidRequest {
        Statements result = Statements.arrayList();
        try (InputStream stream = contentFile.getInputStream();
             ZipInputStream zip = new ZipInputStream(stream)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                Optional<RDFFormat> format =
                        Rio.getParserFormatForFileName(entry.getName());
                if (format.isEmpty()) {
                    // Ignore those that are not RDF.
                    continue;
                }
                // Statements employ rdf4j that would close the stream,
                // we need to prevent this to continue reading zip file.
                result.file().addAll(new IgnoreCloseStream(zip), format.get());
            }
        } catch (IOException | RuntimeException ex) {
            throw new InvalidRequest("Can't read input.", ex);
        }
        return result;
    }

    public Statements loadRdfContent(MultipartFile contentFile)
            throws InvalidRequest {
        return ServletUtilities.read(contentFile);
    }

    private List<ImportPipelineOptions> loadImportPipelineOptions(
            MultipartFile file) throws InvalidRequest {
        Statements statements = ServletUtilities.read(file);
        return RdfToImportPipelineOptions.asImportPipelineOptions(statements);
    }

    private ImportTemplateOptions loadImportTemplateOptions(
            MultipartFile file) throws InvalidRequest {
        Statements statements = ServletUtilities.read(file);
        List<ImportTemplateOptions> candidates =
                RdfToImportTemplateOptions.asImportTemplateOptions(statements);
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        throw new InvalidRequest(
                "Expected one import options got '{}'.",
                candidates.size());
    }

    private ImportTemplate importTemplates(
            MultipartFile optionsFile, Statements statements)
            throws InvalidRequest, ServerError {
        ImportTemplateOptions options = loadImportTemplateOptions(optionsFile);
        ImportTemplate result = new ImportTemplate(
                templateFacade, templateFacade);
        try {
            result.loadFromStatements(statements.selector());
            result.importTemplates(options);
        } catch (StorageException ex) {
            throw new ServerError("Can't import templates.", ex);
        }
        return result;
    }

    public void handleLocalize(
            MultipartFile contentFile, MultipartFile optionsFile,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        Statements statements = ServletUtilities.read(contentFile);
        ImportPipelineOptions pipelineOptions =
                loadImportPipelineOptionForLocalization(optionsFile);
        // Import template.
        ImportTemplate importTemplate =
                importTemplates(optionsFile, statements);
        // Import pipeline.
        ImportPipeline importWorker = new ImportPipeline(
                templateFacade, templateFacade, pipelineFacade,
                importTemplate.getRemoteToLocal());
        Pipeline pipeline;
        try {
            importWorker.loadFromStatements(statements.selector());
            pipeline = importWorker.importPipeline(pipelineOptions);
        } catch (StorageException ex) {
            throw new ServerError("Can't localize pipeline.", ex);
        }
        // Convert pipelines back to RDF.
        Statements result = PipelineToRdf.asRdf(pipeline);
        ServletUtilities.sendResponse(request, response, result);
    }

    private ImportPipelineOptions loadImportPipelineOptionForLocalization(
            MultipartFile optionsFile) throws InvalidRequest {
        List<ImportPipelineOptions> candidates =
                loadImportPipelineOptions(optionsFile);
        ImportPipelineOptions result = new ImportPipelineOptions();
        result.importPipeline = false;
        if (candidates.isEmpty()) {
            // We just use the default values.
        } else if (candidates.size() == 1) {
            // We load some allowed options.
            ImportPipelineOptions givenOptions = candidates.get(0);
            result.targetLabel = givenOptions.targetLabel;
            result.keepPipelineUrl = givenOptions.keepPipelineUrl;
            result.keepPipelineSuffix = givenOptions.keepPipelineSuffix;
            result.targetResource = givenOptions.targetResource;
        } else {
            throw new InvalidRequest(
                    "One options object expected got '{}'.",
                    candidates.size());
        }
        return result;
    }

    public void handleReload(
            HttpServletRequest request, HttpServletResponse response) {
        boolean failed = false;
        try {
            templateFacade.reloadReferenceTemplates();
        } catch (StorageException ex) {
            LOG.error("Can't reload templates.", ex);
            failed = true;
        }
        try {
            pipelineFacade.reloadPipelines();
        } catch (StorageException ex) {
            LOG.error("Can't reload pipelines.", ex);
            failed = true;
        }
        if (failed) {
            response.setStatus(ServletUtilities.HTTP_SERVER_ERROR);
        } else {
            response.setStatus(ServletUtilities.HTTP_OK);
        }
    }

    public void handleGetDesignInformation(
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.sendResponse(
                request, response, assistantService.getDataAsStatements());
    }

    public void handleUnpack(
            MultipartFile pipelineFile, MultipartFile optionsFile,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        Statements pipelineStatements = ServletUtilities.read(pipelineFile);
        Statements optionsStatements = ServletUtilities.read(optionsFile);
        Statements result;
        try {
            result = Statements.wrap(unpackerFacade.unpack(
                    pipelineStatements, optionsStatements));
        } catch (StorageException ex) {
            throw new ServerError("Can't prepare pipeline for execution.", ex);
        }
        ServletUtilities.sendResponse(request, response, result);
    }

}
