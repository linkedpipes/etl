package com.linkedpipes.etl.library.template.reference.migration;

import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;

import java.util.Map;

public class ReferenceTemplateV4 {

    private final Map<Resource, Resource> templateToPlugin;

    public ReferenceTemplateV4(Map<Resource, Resource> templateToPlugin) {
        this.templateToPlugin = templateToPlugin;
    }

    /**
     * Add version information, mapping and core component. Those changes
     * make reference template definition self-contained and allow
     * for per template migration in the future.
     */
    public void migrateToV5(RawReferenceTemplate template)
            throws ReferenceMigrationFailed {
        Resource plugin = templateToPlugin.get(template.template);
        if (plugin == null) {
            throw new ReferenceMigrationFailed(
                    "Missing root template '{}' for '{}'.",
                    template.template, template.resource);
        }
        // Mapping is not new information thus it is added, during
        // reading the information from RDF.
        template.plugin = plugin;
        template.version = 5;
    }

}
