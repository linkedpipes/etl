package com.linkedpipes.etl.storage.distribution;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.template.configuration.ConfigurationFacade;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.ReferenceTemplateLoader;
import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.distribution.model.ImportTemplateOptions;
import com.linkedpipes.etl.storage.template.PluginTemplateFacade;
import com.linkedpipes.etl.storage.template.ReferenceTemplateFacade;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportTemplate {

    public static class Container {

        private final RawReferenceTemplate rawTemplate;

        private Exception exception;

        /**
         * Template as loaded.
         */
        private final ReferenceTemplate loadedTemplate;

        /**
         * Template in local form ready to be stored.
         */
        private ReferenceTemplate localTemplate;

        private boolean storedAsExisting = false;

        private boolean storedAsNew = false;

        public Container(ReferenceTemplateLoader.Container source) {
            this.rawTemplate = source.rawTemplate();
            this.exception = source.exception();
            this.loadedTemplate = source.template();
        }

        public RawReferenceTemplate raw() {
            return rawTemplate;
        }

        public Exception exception() {
            return exception;
        }

        public ReferenceTemplate remote() {
            return loadedTemplate;
        }

        public ReferenceTemplate local() {
            return localTemplate;
        }

        public boolean storedAsExisting() {
            return storedAsExisting;
        }

        public boolean storedAsNew() {
            return storedAsNew;
        }

    }

    private final PluginTemplateFacade pluginFacade;

    private final ReferenceTemplateFacade referenceFacade;

    private final List<Container> containers = new ArrayList<>();

    private final Map<Resource, Resource> remoteToLocal = new HashMap<>();

    public ImportTemplate(
            PluginTemplateFacade pluginFacade,
            ReferenceTemplateFacade referenceFacade) {
        this.pluginFacade = pluginFacade;
        this.referenceFacade = referenceFacade;
    }

    public void loadFromStatements(StatementsSelector statements)
            throws StorageException {
        ReferenceTemplateLoader loader = new ReferenceTemplateLoader(
                pluginFacade.getPluginTemplates().stream()
                        .map(PluginTemplate::resource)
                        .collect(Collectors.toSet()),
                referenceFacade.getTemplateToPluginMap());
        loader.loadAndMigrate(statements);
        loader.getContainers().stream()
                .map(Container::new)
                .forEach(containers::add);
    }

    /**
     * Called after {@link #loadFromStatements(StatementsSelector)}
     * to perform the import. Does not throw is a single template import fail.
     */
    public void importTemplates(ImportTemplateOptions options)
            throws StorageException {
        Map<Resource, Resource> knownMap = buildKnownMap();
        if (options.updateExistingTemplates) {
            updateExistingTemplates(knownMap);
        }
        if (options.importNewTemplates) {
            importNewTemplates(knownMap);
        }
        buildRemoteToLocal(knownMap);
    }

    /**
     * Build mapping of remote resources to local ones using 'known as'.
     */
    private Map<Resource, Resource> buildKnownMap() throws StorageException {
        Map<Resource, Resource> result = new HashMap<>();
        for (ReferenceTemplate template :
                referenceFacade.getReferenceTemplates()) {
            result.put(template.resource(), template.resource());
            if (template.knownAs() != null) {
                result.put(template.knownAs(), template.resource());
            }
        }
        return result;
    }

    private void updateExistingTemplates(Map<Resource, Resource> knownMap) {
        for (Container container : containers) {
            if (container.loadedTemplate == null) {
                continue;
            }
            Resource resource = getLocalResource(knownMap, container);
            if (resource == null) {
                // Not a known template.
                continue;
            }
            try {
                updateExistingTemplate(container, resource);
            } catch (StorageException ex) {
                container.exception = ex;
            }
        }
    }

    private Resource getLocalResource(
            Map<Resource, Resource> knownMap,
            Container container) {
        if (container.localTemplate != null) {
            return container.localTemplate.resource();
        }
        if (container.rawTemplate == null) {
            return null;
        }
        Resource result = knownMap.get(container.rawTemplate.resource);
        if (result == null) {
            // We can also try known as.
            result = knownMap.get(container.rawTemplate.knownAs);
        }
        return result;
    }

    /**
     * Given template localize it for given resources.
     */
    private void updateExistingTemplate(Container container, Resource resource)
            throws StorageException {
        ReferenceTemplate local =
                referenceFacade.getReferenceTemplate(resource);
        PluginTemplate plugin =
                pluginFacade.getPluginTemplate(local.plugin());
        ReferenceTemplate template = container.loadedTemplate;
        ReferenceTemplate nextTemplate = new ReferenceTemplate(
                local.resource(), template.version(),
                local.template(), local.plugin(),
                template.label(), template.description(), template.note(),
                template.color(), template.tags(),
                local.knownAs(),
                updateConfiguration(plugin, template.configuration(), resource),
                local.configurationGraph());
        container.localTemplate = nextTemplate;
        referenceFacade.storeReferenceTemplate(nextTemplate);
        container.storedAsExisting = true;
    }

    private Statements updateConfiguration(
            PluginTemplate plugin, Statements statements, Resource resource) {
        return ConfigurationFacade.localizeConfiguration(
                plugin.configurationDescription(),
                statements.selector(), resource);
    }

    private void importNewTemplates(Map<Resource, Resource> knownMap) {
        boolean runNextRound = true;
        while (runNextRound) {
            runNextRound = false;
            for (Container container : containers) {
                Resource localFromKnown = getLocalResource(knownMap, container);
                if (localFromKnown != null) {
                    // This is a known template, we add a mapping.
                    knownMap.put(container.rawTemplate.resource, localFromKnown);
                    continue;
                }
                Resource local;
                try {
                    local = importNewTemplate(knownMap, container);
                } catch (StorageException ex) {
                    container.exception = ex;
                    continue;
                }
                if (local == null) {
                    // Not ready to import.
                    continue;
                }
                knownMap.put(container.rawTemplate.resource, local);
                runNextRound = true;
            }
        }
    }

    /**
     * Import template and return local resource.
     */
    private Resource importNewTemplate(
            Map<Resource, Resource> knownMap,
            Container container) throws StorageException {
        Resource localResource;
        ReferenceTemplate remote = container.loadedTemplate;
        if (pluginFacade.isPluginTemplate(remote.template())) {
            localResource = remote.template();
        } else {
            // This is not recognized.
            localResource = knownMap.get(remote.template());
        }
        if (localResource == null) {
            // We can not import without parent.
            return null;
        }
        Resource knowAs = remote.knownAs();
        if (remote.knownAs() == null) {
            knowAs = remote.resource();
        }
        PluginTemplate plugin =
                referenceFacade.findPluginTemplate(localResource);
        Resource local = referenceFacade.reserveReferenceResource();
        ReferenceTemplate nextTemplate = new ReferenceTemplate(
                local, remote.version(),
                localResource, remote.plugin(),
                remote.label(), remote.description(), remote.note(),
                remote.color(), remote.tags(),
                knowAs,
                updateConfiguration(plugin, remote.configuration(), local),
                ConfigurationFacade.configurationGraph(local));
        referenceFacade.storeReferenceTemplate(nextTemplate);
        container.localTemplate = nextTemplate;
        container.storedAsNew = true;
        return local;
    }

    private void buildRemoteToLocal(Map<Resource, Resource> knownMap) {
        remoteToLocal.clear();
        for (Container container : containers) {
            Resource remote = container.rawTemplate.resource;
            Resource local = getLocalResource(knownMap, container);
            remoteToLocal.put(remote, local);
        }
    }

    public Map<Resource, Resource> getRemoteToLocal() {
        return Collections.unmodifiableMap(remoteToLocal);
    }

    public List<Container> getContainers() {
        return Collections.unmodifiableList(containers);
    }

}
