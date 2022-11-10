package com.linkedpipes.etl.storage.distribution;

import com.linkedpipes.etl.library.pipeline.adapter.PipelineToRdf;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.adapter.ReferenceTemplateToRdf;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportContent {

    public static final String PIPELINES_DIRECTORY = "pipelines";

    public static final String TEMPLATES_DIRECTORY = "templates";

    private final Map<Resource, Pipeline> pipelinesMap =
            new HashMap<>();

    private final Map<Resource, ReferenceTemplate> templatesMap =
            new HashMap<>();

    private final ExportService exportService;

    private boolean removePrivateConfiguration = false;

    public ExportContent(TemplateFacade templateFacade) {
        this.exportService = new ExportService(templateFacade);
    }

    public void setRemovePrivateConfiguration(boolean value) {
        removePrivateConfiguration = value;
    }

    public void addPipelines(List<Pipeline> pipelines) {
        for (Pipeline pipeline : pipelines) {
            this.pipelinesMap.put(pipeline.resource(), pipeline);
        }
    }

    public void addTemplates(List<ReferenceTemplate> templates) {
        for (ReferenceTemplate template : templates) {
            this.templatesMap.put(template.resource(), template);
        }
    }

    public Statements exportStatements()
            throws StorageException {
        List<Pipeline> pipelines = preparePipelinesForExport();
        List<ReferenceTemplate> templates = prepareTemplatesForExport();
        Statements result = Statements.arrayList();
        for (Pipeline pipeline : pipelines) {
            result.addAll(PipelineToRdf.asRdf(pipeline));
        }
        for (ReferenceTemplate template : templates) {
            result.addAll(ReferenceTemplateToRdf.asRdf(template));
        }
        return result;
    }

    public List<Pipeline> preparePipelinesForExport()
            throws StorageException {
        List<Pipeline> result = new ArrayList<>(pipelinesMap.size());
        for (Pipeline pipeline : pipelinesMap.values()) {
            if (removePrivateConfiguration) {
                pipeline = exportService.removePrivateConfiguration(pipeline);
            }
            result.add(pipeline);
        }
        return result;
    }

    public List<ReferenceTemplate> prepareTemplatesForExport()
            throws StorageException {
        Map<Resource, ReferenceTemplate> collected = new HashMap<>();
        for (ReferenceTemplate template : templatesMap.values()) {
            collected.put(template.resource(), template);
        }
        for (Pipeline pipeline : pipelinesMap.values()) {
            for (ReferenceTemplate template :
                    exportService.collectTemplates(pipeline)) {
                if (collected.containsKey(template.resource())) {
                    continue;
                }
                collected.put(template.resource(), template);
            }
        }
        List<ReferenceTemplate> result = new ArrayList<>(collected.size());
        for (ReferenceTemplate template : collected.values()) {
            if (removePrivateConfiguration) {
                template = exportService.removePrivateConfiguration(template);
            }
            result.add(template);
        }
        return result;
    }

    public void exportZip(
            OutputStream stream,
            Function<Pipeline, String> namePipeline,
            Function<ReferenceTemplate, String> nameTemplate)
            throws StorageException {
        List<Pipeline> pipelines = preparePipelinesForExport();
        List<ReferenceTemplate> templates = prepareTemplatesForExport();
        //
        ZipOutputStream zip = new ZipOutputStream(
                stream, StandardCharsets.UTF_8);
        try {
            Set<String> pipelineNames = new HashSet<>();
            for (Pipeline pipeline : pipelines) {
                String name = namePipeline.apply(pipeline);
                while (pipelineNames.contains(name)) {
                    name += " [COLLISION]";
                }
                pipelineNames.add(name);
                pipelineZipEntry(zip, pipeline, name);
            }
            Set<String> templateNames = new HashSet<>();
            for (ReferenceTemplate template : templates) {
                String name = nameTemplate.apply(template);
                while (templateNames.contains(name)) {
                    name += " [COLLISION]";
                }
                templateNames.add(name);
                templateZipEntry(zip, template, name);
            }
            zip.close();
        } catch (IOException ex) {
            throw new StorageException("Can't create archive.", ex);
        }
    }

    private void pipelineZipEntry(
            ZipOutputStream zip, Pipeline pipeline, String name)
            throws IOException {
        Statements statements = PipelineToRdf.asRdf(pipeline);
        ZipEntry entry = new ZipEntry(
                PIPELINES_DIRECTORY + "/" + name + ".trig");
        zip.putNextEntry(entry);
        statements.file().writeToStream(zip, RDFFormat.TRIG);
        zip.closeEntry();
    }


    private void templateZipEntry(
            ZipOutputStream zip, ReferenceTemplate template, String name)
            throws IOException {
        Statements statements = ReferenceTemplateToRdf.asRdf(template);
        ZipEntry entry = new ZipEntry(
                TEMPLATES_DIRECTORY + "/" + name + ".trig");
        zip.putNextEntry(entry);
        statements.file().writeToStream(zip, RDFFormat.TRIG);
        zip.closeEntry();
    }

}
