package com.linkedpipes.etl.storage.pipeline.transformation;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.migration.MigrateV1ToV2;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.template.mapping.Mapping;
import com.linkedpipes.etl.storage.template.mapping.MappingFacade;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// TODO We need to do template migration here.
class ImportTemplates {

    private static final Logger LOG = LoggerFactory.getLogger(ImportTemplates.class);

    private final TemplateFacade templateFacade;

    private final MappingFacade mappingFacade;

    private boolean importMissing = false;

    private boolean updateExisting = false;

    /**
     * Use to migrate configuration, change from version 1 to 2.
     */
    private boolean migrateConfigurations = false;

    private Resource pipelineGraph;

    private Map<IRI, List<Statement>> graphs;

    private Mapping mapping;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public ImportTemplates(TemplateFacade templateFacade,
                           MappingFacade mappingFacade) {
        this.templateFacade = templateFacade;
        this.mappingFacade = mappingFacade;
    }

    public void setImportMissing(boolean importMissing) {
        this.importMissing = importMissing;
    }

    public void setUpdateExisting(boolean updateExisting) {
        this.updateExisting = updateExisting;
    }

    public void setMigrateConfigurations(boolean migrateConfigurations) {
        this.migrateConfigurations = migrateConfigurations;
    }

    /**
     * Align templates in the pipeline with local templates. Missing
     * templates are imported.
     */
    public Collection<Statement> importTemplates(
            Collection<Statement> pipelineRdf) throws BaseException {
        LOG.debug("Import options:");
        LOG.debug("  Import templates: {}", this.updateExisting);
        LOG.debug("  Update existing templates: {}",this.updateExisting);
        LOG.debug("  Migrate configurations: {}",this.migrateConfigurations);
        initialize();
        loadStatements(pipelineRdf);
        if (this.pipelineGraph == null) {
            // There is no pipeline.
            LOG.warn("No pipeline graph found!");
            return pipelineRdf;
        }
        this.loadMapping(pipelineRdf);
        this.importTemplates();
        this.saveMappingsToHdd();
        return this.collectPipeline();
    }

    private void initialize() {
        this.pipelineGraph = null;
        this.graphs = new HashMap<>();
        this.mapping = null;
    }

    private void loadStatements(Collection<Statement> pipelineRdf) {
        for (Statement statement : pipelineRdf) {
            List<Statement> graph = this.graphs.get(statement.getContext());
            if (graph == null) {
                graph = new LinkedList<>();
                this.graphs.put((IRI) statement.getContext(), graph);
            }
            if (statement.getPredicate().equals(RDF.TYPE) &&
                    statement.getObject().equals(Pipeline.TYPE)) {
                this.pipelineGraph = statement.getContext();
            }
            graph.add(statement);

        }
    }

    private void loadMapping(Collection<Statement> pipelineRdf) {
        this.mapping = this.mappingFacade.createMappingFromStatements(
                pipelineRdf);
    }

    private void importTemplates() throws BaseException {
        List<TemplateInfo> templates = TemplateInfo.create(this.graphs);
        List<TemplateInfo> resolvedTemplates = new ArrayList<>();
        // We try to import templates. As there might be hierarchy we should
        // import at least one template in each cycle.
        while (!templates.isEmpty()) {
            for (TemplateInfo templateInfo : templates) {
                if (resolveTemplate(templateInfo)) {
                    resolvedTemplates.add(templateInfo);
                }
            }
            if (resolvedTemplates.isEmpty()) {
                LOG.error("Failed to import following templates:");
                for (TemplateInfo templateInfo : templates) {
                    LOG.info("{}", templateInfo.getIri());
                }
                return;
            }
            templates.removeAll(resolvedTemplates);
            resolvedTemplates.clear();
        }
    }

    private boolean resolveTemplate(TemplateInfo template)
            throws BaseException {
        String templateIri = this.mapping.toLocal(template.getIri());
        Template localTemplate = this.templateFacade.getTemplate(templateIri);
        if (localTemplate == null) {
            if (this.importMissing) {
                return importTemplate(template);
            } else {
                LOG.info("Skip: {}", templateIri);
                return false;
            }
        } else {
            if (this.updateExisting) {
                LOG.info("Updating local template: {}", templateIri);
                updateLocal(template, localTemplate);
            }
            return true;
        }
    }

    private void updateLocal(TemplateInfo remote, Template local)
            throws BaseException {
        this.templateFacade.updateInterface(local, remote.getDefinition());
        Template parent = this.templateFacade.getParent(local);
        Collection<Statement> config = prepareConfiguration(remote, parent);
        this.templateFacade.updateConfig(local, config);
    }

    private Collection<Statement> prepareConfiguration(
            TemplateInfo remote, Template localParent) {
        if (!this.migrateConfigurations) {
            return remote.getConfiguration();
        }
        Template root = this.templateFacade.getRootTemplate(localParent);
        if (MigrateV1ToV2.shouldUpdate(root.getIri())) {
            return MigrateV1ToV2.updateConfiguration(
                    remote.getConfiguration(),
                    root.getIri());
        } else {
            return remote.getConfiguration();
        }
    }

    private boolean importTemplate(TemplateInfo remoteTemplate) {
        Template parent = getLocalParent(remoteTemplate);
        if (parent == null) {
            return false;
        }
        LOG.info("Importing: {} with local parent: {}",
                remoteTemplate.getIri(), parent.getIri());
        remoteTemplate.setTemplate(this.valueFactory.createIRI(parent.getIri()));
        try {
            Template template = this.templateFacade.createTemplate(
                    remoteTemplate.getDefinition(),
                    prepareConfiguration(remoteTemplate, parent));
            String originalTemplateIri =
                    this.mapping.toOriginal(remoteTemplate.getIri());
            this.mappingFacade.add(template, originalTemplateIri);
        } catch (BaseException ex) {
            LOG.error("Can't import template: {}", remoteTemplate.getIri(), ex);
            LOG.info("Template is ignored.");
            return true;
        }
        return true;
    }

    private Template getLocalParent(TemplateInfo remoteTemplate) {
        String parentIri = remoteTemplate.getTemplate().stringValue();
        String localParentIri = mapping.toLocal(parentIri);
        return templateFacade.getTemplate(localParentIri);
    }

    private void saveMappingsToHdd() {
        mappingFacade.save();
    }

    private List<Statement> collectPipeline() {
        List<Statement> definition = this.graphs.get(this.pipelineGraph);
        Set<Resource> configurations = new HashSet<>();
        List<Statement> toRemove = new ArrayList<>();
        List<Statement> toAdd = new ArrayList<>();
        for (Statement statement : definition) {
            // Check for configuration.
            String predicate = statement.getPredicate().stringValue();
            if (predicate.equals(LP_PIPELINE.HAS_CONFIGURATION_GRAPH)) {
                configurations.add((Resource) statement.getObject());
                continue;
            }
            // Check template references and update them.
            if (predicate.equals(LP_PIPELINE.HAS_TEMPLATE)) {
                // Check for import. Now all templates should be imported
                // so we can just ask for mapping.
                String localTemplateIri = this.mapping.toLocal(
                        statement.getObject().stringValue());
                toRemove.add(statement);
                toAdd.add(this.valueFactory.createStatement(
                        statement.getSubject(),
                        statement.getPredicate(),
                        this.valueFactory.createIRI(localTemplateIri),
                        statement.getContext()));
            }
        }
        // Collect.
        List<Statement> result = new ArrayList<>(definition.size());
        result.addAll(definition);
        result.removeAll(toRemove);
        result.addAll(toAdd);
        for (Resource configurationIri : configurations) {
            result.addAll(this.graphs.getOrDefault(
                    configurationIri, Collections.EMPTY_LIST));
        }
        return result;
    }

}
