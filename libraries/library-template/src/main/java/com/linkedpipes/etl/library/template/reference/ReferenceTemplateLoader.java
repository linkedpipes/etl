package com.linkedpipes.etl.library.template.reference;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.adapter.RdfToRawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.migration.MigrateReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.migration.ReferenceMigrationFailed;
import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Given statements load the reference templates. Perform migration
 * where needed. Collect loaded templates and exceptions.
 */
public class ReferenceTemplateLoader {

    public static class Container {

        private final RawReferenceTemplate rawTemplate;

        private Exception exception;

        private ReferenceTemplate template;

        public Container(RawReferenceTemplate rawReferenceTemplate) {
            this.rawTemplate = rawReferenceTemplate;
        }

        public RawReferenceTemplate rawTemplate() {
            return rawTemplate;
        }

        public ReferenceTemplate template() {
            return template;
        }

        public Exception exception() {
            return exception;
        }

        public boolean isFailed() {
            return exception != null;
        }

        public boolean isMigrated() {
            return !isFailed() && rawTemplate.version != template.version();
        }

    }

    /**
     * List of available plugins.
     */
    private final Set<Resource> plugins;

    /**
     * For each template store its plugin.
     */
    private final Map<Resource, Resource> templateToPlugin;

    private final List<Container> containers = new ArrayList<>();

    public ReferenceTemplateLoader(
            Set<Resource> plugins, Map<Resource, Resource> templateToPlugin) {
        this.plugins = plugins;
        this.templateToPlugin = new HashMap<>(templateToPlugin);
    }

    /**
     * Load from statements.
     */
    public void loadAndMigrate(Statements statements) {
        List<RawReferenceTemplate> rawTemplates =
                RdfToRawReferenceTemplate.asRawReferenceTemplates(
                        statements.selector());
        loadAndMigrate(rawTemplates);
    }

    /**
     * Load from list of raw references.
     */
    public void loadAndMigrate(List<RawReferenceTemplate> rawTemplates) {
        updateTemplateToPlugin(rawTemplates);
        MigrateReferenceTemplate migration =
                new MigrateReferenceTemplate(templateToPlugin);
        for (RawReferenceTemplate rawTemplate : rawTemplates) {
            Container container = new Container(rawTemplate);
            containers.add(container);
            try {
                container.template = migration.migrate(rawTemplate);
            } catch (ReferenceMigrationFailed ex) {
                container.exception = ex;
            }
        }
    }

    private void updateTemplateToPlugin(List<RawReferenceTemplate> templates) {
        // Naive implementation, yet as there should not be many templates
        // we should be fine.
        boolean change = true;
        while (change) {
            change = false;
            for (RawReferenceTemplate template : templates) {
                if (templateToPlugin.containsKey(template.resource)) {
                    continue;
                }
                if (template.plugin != null) {
                    templateToPlugin.put(template.resource, template.plugin);
                    change = true;
                    continue;
                }
                Resource plugin = templateToPlugin.getOrDefault(
                        template.template, template.template);
                if (plugins.contains(plugin)) {
                    templateToPlugin.put(template.resource, plugin);
                    // Also add self reference for the plugin.
                    templateToPlugin.put(plugin, plugin);
                    change = true;
                }
            }
        }
    }

    public List<Container> getContainers() {
        return Collections.unmodifiableList(containers);
    }

    public List<ReferenceTemplate> getTemplates() {
        return containers.stream()
                .filter(container -> !container.isFailed())
                .map(Container::template)
                .toList();
    }

    public List<ReferenceTemplate> getMigratedTemplates() {
        return containers.stream()
                .filter(Container::isMigrated)
                .map(Container::template)
                .toList();
    }

    public boolean hasAnyFailed() {
        for (Container container : containers) {
            if (container.isFailed()) {
                return true;
            }
        }
        return false;
    }

}
