package com.linkedpipes.etl.library.template.reference;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.adapter.rdf.RdfToRawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.migration.MigrateReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.migration.ReferenceMigrationFailed;
import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReferenceTemplateLoader {

    /**
     * List of available plugins.
     */
    private final Set<Resource> plugins;

    /**
     * For each template store its plugin.
     */
    private final Map<Resource, Resource> templateToPlugin;

    public ReferenceTemplateLoader(
            Set<Resource> plugins,
            Map<Resource, Resource> templateToPlugin) {
        this.plugins = plugins;
        this.templateToPlugin = new HashMap<>(templateToPlugin);
    }

    public List<ReferenceTemplate> load(Statements statements)
            throws ReferenceMigrationFailed {
        List<RawReferenceTemplate> rawTemplates =
                RdfToRawReferenceTemplate.asRawReferenceTemplates(
                        statements.selector());
        updateTemplateToPlugin(rawTemplates);
        List<ReferenceTemplate> result = new ArrayList<>(rawTemplates.size());
        MigrateReferenceTemplate migration =
                new MigrateReferenceTemplate(templateToPlugin);
        for (RawReferenceTemplate rawTemplate : rawTemplates) {
            result.add(migration.migrate(rawTemplate));
        }
        return result;
    }

    private void updateTemplateToPlugin(List<RawReferenceTemplate> templates) {
        // Naive implementation, yet as there should not be many templates
        // we should be fine.
        boolean change = true;
        while (change) {
            change = false;
            for (RawReferenceTemplate template : templates) {
                if (template.plugin != null) {
                    templateToPlugin.put(template.resource, template.plugin);
                    change = true;
                    continue;
                }
                Resource plugin = templateToPlugin.getOrDefault(
                        template.template, template.template);
                if (plugins.contains(plugin)) {
                    templateToPlugin.put(template.resource, plugin);
                    change = true;
                }
            }
        }
    }

}
