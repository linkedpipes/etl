package com.linkedpipes.etl.library.template.reference.migration;

import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
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
    public ReferenceTemplate migrateToV5(ReferenceTemplate template)
            throws MigrationFailed {
        Resource pluginTemplate = templateToPlugin.get(template.resource());
        if (pluginTemplate == null) {
            throw new MigrationFailed(
                    "Missing root template '{}' for '{}'.",
                    template.template(),
                    template.resource());
        }
        // Mapping is not new information thus it is added, during
        // reading the information from RDF.
        return new ReferenceTemplate(
                template.resource(),
                template.template(),
                template.label(),
                template.description(),
                template.note(),
                template.color(),
                template.tags(),
                template.knownAs(),
                pluginTemplate,
                5,
                template.configuration(),
                template.configurationGraph());
    }

}
