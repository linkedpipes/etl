package com.linkedpipes.etl.storage.distribution;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.configuration.ConfigurationFacade;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.distribution.model.ExportPipelineOptions;
import com.linkedpipes.etl.storage.distribution.model.FullPipeline;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ExportPipeline {

    private final ExportService exportService;

    public ExportPipeline(TemplateFacade referenceFacade) {
        this.exportService = new ExportService(referenceFacade);
    }

    public FullPipeline export(Pipeline pipeline, ExportPipelineOptions options)
            throws StorageException {

        List<ReferenceTemplate> templates = Collections.emptyList();
        if (options.includeTemplate) {
            templates = exportService.collectTemplates(pipeline);
        }
        if (options.removePrivateConfiguration) {
            pipeline = exportService.removePrivateConfiguration(pipeline);
            templates = exportService.removePrivateConfiguration(templates);
        }
        return new FullPipeline(pipeline, templates);
    }

}
