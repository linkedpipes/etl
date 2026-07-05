package com.linkedpipes.etl.storage.distribution;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.distribution.model.ExportPipelineOptions;
import com.linkedpipes.etl.storage.distribution.model.FullPipeline;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import java.util.Collections;
import java.util.List;

public class ExportPipeline {

    private final ExportService exportService;

    public ExportPipeline(TemplateFacade referenceFacade) {
        this.exportService = new ExportService(referenceFacade);
    }

    public FullPipeline export(Pipeline pipeline, ExportPipelineOptions options) throws StorageException {

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
