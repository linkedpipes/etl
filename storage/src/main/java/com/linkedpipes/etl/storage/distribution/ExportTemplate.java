package com.linkedpipes.etl.storage.distribution;

import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.TemplateFacade;

import java.util.ArrayList;
import java.util.List;

public class ExportTemplate {

    private final ExportService exportService;

    public ExportTemplate(TemplateFacade referenceFacade) {
        this.exportService = new ExportService(referenceFacade);
    }

    /**
     * Does not add new templates.
     */
    public List<ReferenceTemplate> export(
            List<ReferenceTemplate> templates,
            boolean removePrivateConfiguration)
            throws StorageException {
        List<ReferenceTemplate> result = new ArrayList<>(templates);
        if (removePrivateConfiguration) {
            result = exportService.removePrivateConfiguration(templates);
        }
        return result;
    }

}
