package com.linkedpipes.etl.storage.web.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.configuration.ConfigurationFacade;
import com.linkedpipes.etl.library.template.configuration.adapter.rdf.RdfToConfigurationDescription;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.PipelineRef;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.template.ReferenceTemplateRef;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping(value = "/export")
public class ExportServlet {

    private static final String EXPORT_NONE = "none";

    private static final String EXPORT_ALL = "all";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private PipelineFacade pipelines;

    @Autowired
    private TemplateFacade templates;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public void export(
            @RequestParam(name = "templates") String exportTemplates,
            @RequestParam(name = "pipelines") String exportPipelines,
            @RequestParam(name = "removePrivateConfig", defaultValue = "false")
                    boolean removePrivateConfig,
            HttpServletResponse response)
            throws IOException, StorageException {
        List<PipelineRef> pipelines = collectPipelines(
                createFilter(exportPipelines));
        List<ReferenceTemplateRef> templates = collectTemplates(
                createFilter(exportTemplates));
        response.setHeader("Content-Type", "application/zip");
        try (OutputStream stream = response.getOutputStream()) {
            ZipOutputStream zip = new ZipOutputStream(
                    stream, StandardCharsets.UTF_8);

            for (PipelineRef pipeline : pipelines) {
                pipelineEntry(zip, removePrivateConfig, pipeline);
            }
            for (ReferenceTemplateRef template : templates) {
                templateEntry(zip, removePrivateConfig, template);
            }

            zip.close();
        }
    }

    private Predicate<String> createFilter(String value) throws StorageException {
        if (value == null || EXPORT_NONE.equals(value)) {
            return (iri) -> false;
        }
        if (EXPORT_ALL.equals(value)) {
            return (iri) -> true;
        }
        List<String> positive = parseAsJsonArray(value);
        return positive::contains;
    }

    private List<String> parseAsJsonArray(String value) throws StorageException {
        JsonNode root;
        try {
            root = mapper.readTree(value);
        } catch (IOException ex) {
            throw new StorageException("Can't parse query: {}", value);
        }
        if (!(root instanceof ArrayNode)) {
            throw new StorageException("Can't parse query: {}", value);
        }
        List<String> result = new ArrayList<>();
        for (JsonNode node : root) {
            result.add(node.textValue());
        }
        return result;
    }

    private List<PipelineRef> collectPipelines(Predicate<String> predicate) {
        return pipelines.getService().stream()
                .filter(pipeline -> predicate.test(pipeline.getIri()))
                .toList();
    }

    private List<ReferenceTemplateRef> collectTemplates(
            Predicate<String> predicate) {
        return templates.getTemplates().stream()
                .filter(item -> predicate.test(item.getIri()))
                .filter(item -> item instanceof ReferenceTemplateRef)
                .map(item -> (ReferenceTemplateRef) item)
                .toList();

    }

    private void pipelineEntry(
            ZipOutputStream zip, boolean removePrivateConfig,
            PipelineRef pipeline) throws StorageException, IOException {
        Collection<Statement> statements = pipelines.getPipelineRdf(
                pipeline, false, false, removePrivateConfig);
        String suffix = pipeline.getIri().substring(
                pipeline.getIri().lastIndexOf("/") + 1);
        ZipEntry entry = new ZipEntry("pipelines/" + suffix + ".trig");
        zip.putNextEntry(entry);
        writeStatementsAsTrig(statements, zip);
        zip.closeEntry();
    }

    private void writeStatementsAsTrig(
            Collection<Statement> statements, OutputStream stream) {
        RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, stream);
        writer.startRDF();
        for (Statement s : statements) {
            writer.handleStatement(s);
        }
        writer.endRDF();
    }

    private void templateEntry(
            ZipOutputStream zip, boolean removePrivateConfig,
            ReferenceTemplateRef template)
            throws StorageException, IOException {
        String directory = "templates/" + template.getIri().substring(
                template.getIri().lastIndexOf("/") + 1) + "/";

        Collection<Statement> configuration = templates.getConfig(template);
        if (removePrivateConfig) {
            configuration = removePrivateConfiguration(template, configuration);
        }

        zip.putNextEntry(new ZipEntry(directory + "configuration.trig"));
        writeStatementsAsTrig(configuration, zip);
        zip.closeEntry();

        zip.putNextEntry(new ZipEntry(directory + "definition.trig"));
        writeStatementsAsTrig(templates.getDefinition(template), zip);
        zip.closeEntry();

        zip.putNextEntry(new ZipEntry(directory + "interface.trig"));
        writeStatementsAsTrig(templates.getInterface(template), zip);
        zip.closeEntry();
    }

    private Collection<Statement> removePrivateConfiguration(
            ReferenceTemplateRef template, Collection<Statement> configuration)
            throws StorageException {
        List<ConfigurationDescription> candidateDescription =
        RdfToConfigurationDescription.asConfigurationDescriptions(
                Statements.wrap(
                        templates.getConfigurationDescription(
                                template.getIri())).selector());
        if (candidateDescription.size() != 1) {
            throw new StorageException(
                    "Invalid configuration description for {}",
                    template.getIri());
        }
        return ConfigurationFacade.removePrivateStatements(
                Statements.wrap(configuration).selector(),
                candidateDescription.get(0));
    }

}
