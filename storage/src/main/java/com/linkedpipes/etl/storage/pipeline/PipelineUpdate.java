package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.mapping.Mapping;
import com.linkedpipes.etl.storage.mapping.MappingFacade;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.rdf.StatementsCollection;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Provide functionality to perform updates on the pipeline.
 */
public class PipelineUpdate {

    private static final Logger LOG
            = LoggerFactory.getLogger(PipelineUpdate.class);

    public static final IRI COMPONENT;

    public static final IRI HAS_TEMPLATE;

    public static class UpdateFailed extends BaseException {

        public UpdateFailed(String message, Object... args) {
            super(message, args);
        }

    }

    static {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        COMPONENT = vf.createIRI(
                "http://linkedpipes.com/ontology/Component");
        HAS_TEMPLATE = vf.createIRI(
                "http://linkedpipes.com/ontology/template");
    }

    private PipelineUpdate() {
    }

    /**
     * Perform changes on the pipeline related to change in version.
     *
     * During import there might be issues with missing templates, etc .. these
     * are not errors.
     *
     * @param pipelineRdf
     * @param templateFacade
     * @param throwOnWarning If true raise an exception in case of 'warning'.
     * @return Updated pipeline.
     */
    public static Collection<Statement> migrate(
            Collection<Statement> pipelineRdf, TemplateFacade templateFacade,
            boolean throwOnWarning) throws UpdateFailed {
        // Split pipeline into graphs and locate pipeline resource.
        final Resource pplResource = RdfUtils.find(pipelineRdf,
                Pipeline.TYPE);
        if (pipelineRdf == null) {
            throw new UpdateFailed("Missing pipeline resource.");
        }
        final StatementsCollection all = new StatementsCollection(pipelineRdf);
        final StatementsCollection configurations = all.filter(
                (s) -> !s.getContext().equals(pplResource));
        final RdfObjects pplObject = new RdfObjects(all.filter(
                (s) -> s.getContext().equals(pplResource)).getStatements());
        final RdfObjects.Entity pipeline = pplObject.getTypeSingle(
                Pipeline.TYPE);
        // Load version.
        int version;
        try {
            final Value value = pipeline.getProperty(Pipeline.HAS_VERSION);
            version = ((Literal) value).intValue();
        } catch (Exception ex) {
            version = 0;
        }
        LOG.info("Migrating pipeline '{}' version '{}'", pplResource, version);
        // Perform update.
        switch (version) {
            case 0:
                migrateFrom_0(pplObject, templateFacade, throwOnWarning);
            case 1: // Current version.
                break;
            default:
                throw new UpdateFailed("Invalid version: {}", version);
        }
        // Replace information about version.
        pipeline.delete(Pipeline.HAS_VERSION);
        pipeline.add(Pipeline.HAS_VERSION, Pipeline.VERSION_NUMBER);
        // Create output representation.
        final List<Statement> output = new LinkedList<>();
        output.addAll(pplObject.asStatements(pplResource));
        output.addAll(configurations.getStatements());
        return output;
    }

    /**
     * Perform inplace update from version 0 to version 1. Does not change
     * pipeline version property.
     *
     * @param pipeline
     * @param templateFacade
     * @param throwOnMissing If true and template is missing then throw.
     */
    private static void migrateFrom_0(RdfObjects pipeline,
            TemplateFacade templateFacade, boolean throwOnMissing)
            throws UpdateFailed {
        // Example of conversion:
        // http://localhost:8080/resources/components/t-tabular
        // http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0
        final ValueFactory vf = SimpleValueFactory.getInstance();
        for (RdfObjects.Entity entity : pipeline.getTyped(COMPONENT)) {
            final List<Resource> newTemplates = new LinkedList<>();
            for (RdfObjects.Entity ref :
                    entity.getReferences(HAS_TEMPLATE)) {
                String templateIri = ref.getResource().stringValue();
                // The extracted name is /t-tabular and we add / to the end
                // to prevent t-tabular to match t-tabularUv.
                String name = templateIri.substring(
                        templateIri.lastIndexOf("/")) + "/";
                // We need to search for components to match the name.
                boolean templateFound = false;
                for (Template template : templateFacade.getTemplates()) {
                    if (template.getIri().contains(name)) {
                        templateIri = template.getIri();
                        templateFound = true;
                        break;
                    }
                }
                if (!templateFound && throwOnMissing) {
                    // We are missing a template.
                    throw new UpdateFailed("Missing template: {}", templateIri);
                }
                newTemplates.add(vf.createIRI(templateIri));
            }
            ;
            entity.deleteReferences(HAS_TEMPLATE);
            newTemplates.forEach((e) -> entity.add(HAS_TEMPLATE, e));
        }
    }

    /**
     * Update resource/graph IRIs.
     *
     * @param pipelineRdf
     * @param baseIri
     * @return
     */
    public static Collection<Statement> updateResources(
            Collection<Statement> pipelineRdf, String baseIri) {
        // Create mapping.
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final Map<Resource, Resource> mapping = new HashMap<>();
        for (Statement s : pipelineRdf) {
            // Create mapping for all typed resources.
            if (s.getPredicate().equals(RDF.TYPE)) {
                if (s.getObject().equals(Pipeline.TYPE)) {
                    // For pipeline we the IRI as it is, event if
                    // we should overide existing from the graph.
                    mapping.put(s.getSubject(), valueFactory.createIRI(
                            baseIri));
                } else if (!mapping.containsKey(s.getSubject())) {
                    // Only if the mapping is missing.
                    mapping.put(s.getSubject(), valueFactory.createIRI(
                            baseIri + "/" + (mapping.size() + 1)));
                }
            }
            // And for all graphs. This is needed as names
            // of configuration graphs may not be same as the name of
            // configuration resource.
            if (!mapping.containsKey(s.getContext())) {
                mapping.put(s.getContext(), valueFactory.createIRI(
                        baseIri + "/graph/" + (mapping.size() + 1)));
            }
        }
        // Update statements.
        final List<Statement> result = new ArrayList<>(pipelineRdf.size());
        for (Statement s : pipelineRdf) {
            final Resource subject = mapping.getOrDefault(
                    s.getSubject(), s.getSubject());
            Value object = mapping.get(s.getObject());
            if (object == null) {
                object = s.getObject();
            }
            final Resource context = mapping.getOrDefault(
                    s.getContext(), s.getContext());
            //
            result.add(valueFactory.createStatement(
                    subject, s.getPredicate(), object, context));
        }
        return result;
    }

    /**
     * Perform in-place update of pipeline labels.
     *
     * @param pipelineRdf Pipeline in the RDF.
     * @param pipelineIri
     * @param labels
     * @return
     */
    public static Collection<Statement> updateLabels(
            Collection<Statement> pipelineRdf, IRI pipelineIri,
            Collection<Literal> labels) {
        // Add all besides the pipeline labels.
        final List<Statement> result = new ArrayList<>(pipelineRdf.size());
        for (Statement statement : pipelineRdf) {
            if (!statement.getSubject().equals(pipelineIri) ||
                    !SKOS.PREF_LABEL.equals(statement.getPredicate())) {
                result.add(statement);
            }
        }
        // Add labels.
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        for (Value value : labels) {
            result.add(valueFactory.createStatement(pipelineIri,
                    SKOS.PREF_LABEL, value, pipelineIri));
        }
        return result;
    }

    /**
     * Align templates in the pipeline with local templates. Missing
     * templates are imported.
     *
     * @param pipelineRdf
     * @param templateFacade
     * @param mappingFacade
     * @param importMissing
     * @param updateExisting
     * @return
     */
    public static Collection<Statement> updateTemplates(
            Collection<Statement> pipelineRdf, TemplateFacade templateFacade,
            MappingFacade mappingFacade, boolean importMissing,
            boolean updateExisting) throws BaseException {
        LOG.info("import: {} update: {}", importMissing, updateExisting);
        // First we need to split pipeline statements based on their graph.
        final Map<IRI, List<Statement>> graphs = new HashMap<>();
        Resource pipelineGraph = null;
        for (Statement statement : pipelineRdf) {
            List<Statement> graph = graphs.get(statement.getContext());
            if (graph == null) {
                graph = new LinkedList<>();
                graphs.put((IRI) statement.getContext(), graph);
            }
            if (statement.getPredicate().equals(RDF.TYPE) &&
                    statement.getObject().equals(Pipeline.TYPE)) {
                pipelineGraph = statement.getContext();
            }
            graph.add(statement);
        }
        if (pipelineGraph == null) {
            // There is nothing to import.
            return pipelineRdf;
        }
        // Read mappings if there are any.
        final Mapping mapping = mappingFacade.read(pipelineRdf);
        // Iterate over all templates. For each template check if the
        // template is known. If no check if parent is known. If so
        // import the template.
        final ValueFactory vf = SimpleValueFactory.getInstance();
        List<TemplateInfo> templates = TemplateInfo.create(graphs);
        while (!templates.isEmpty()) {
            final Collection<TemplateInfo> toRemove = new LinkedList<>();
            for (TemplateInfo templateInfo : templates) {
                // Translate IRI.
                final String templateIri = mapping.remoteToLocal(
                        templateInfo.getIri());
                // Check if we know the template.
                final Template localTemplate = templateFacade.getTemplate(
                        templateIri);
                if (localTemplate != null) {
                    if (updateExisting) {
                        LOG.info("updating: {}", templateIri);
                        templateFacade.updateTemplate(localTemplate,
                                templateInfo.getDefinition());
                        templateFacade.updateConfig(localTemplate,
                                templateInfo.getConfiguration());
                    }
                    toRemove.add(templateInfo);
                    continue;
                }
                // Continue only if we should import.
                if (!importMissing) {
                    LOG.info("skip: {}", templateIri);
                    continue;
                }
                // We import the hierarchy from the top, so for every
                // template to import we require its parent to be
                // already imported.
                String parentIri = templateInfo.getTemplate().stringValue();
                String localParentIri = mapping.remoteToLocal(parentIri);
                final Template parent = templateFacade.getTemplate(
                        localParentIri);
                if (parent == null) {
                    continue;
                }
                LOG.info("import: {}", templateIri);
                // Update the parent reference.
                templateInfo.setTemplate(vf.createIRI(parent.getIri()));
                //
                try {
                    // Create new template from the templateInfo -> import.
                    Template template = templateFacade.createTemplate(
                            templateInfo.getDefinition(),
                            templateInfo.getConfiguration());
                    // Add mapping, so it can be used by other templates.
                    LOG.info(" --> {}", template.getIri());
                    mappingFacade.add(template, mapping.toOriginal(
                            templateInfo.getIri()));
                } catch (BaseException ex) {
                    // TODO Can't create template, for now skip the template.
                    LOG.info("", ex);
                    continue;
                }
                toRemove.add(templateInfo);
            }
            templates.removeAll(toRemove);
            if (toRemove.isEmpty()) {
                // TODO We failed to import any template.
                // Could we be missing dependencies?
                LOG.error("There might be missing templates!");
                for (TemplateInfo templateInfo : templates) {
                    LOG.info("{}", templateInfo.getIri());
                }
                break;
            }
        }
        // Save mappings.
        mappingFacade.save();
        // Update templates in pipeline.
        final List<Statement> pipelineDefinition = graphs.get(pipelineGraph);
        final Set<Resource> configurations = new HashSet<>();
        final List<Statement> toRemove = new ArrayList<>();
        final List<Statement> toAdd = new ArrayList<>();
        for (Statement statement : pipelineDefinition) {
            // Check for configuration.
            if (statement.getPredicate().stringValue().equals(
                    "http://linkedpipes.com/ontology/configurationGraph")) {
                configurations.add((Resource) statement.getObject());
                continue;
            }
            // Check template references and update them.
            if (statement.getPredicate().stringValue().equals(
                    "http://linkedpipes.com/ontology/template")) {
                // Check for import. Now all templates should be imported
                // so we can just ask for mapping.
                String localTemplateIri = mapping.remoteToLocal(
                        statement.getObject().stringValue());
                toRemove.add(statement);
                toAdd.add(vf.createStatement(statement.getSubject(),
                        statement.getPredicate(),
                        vf.createIRI(localTemplateIri),
                        statement.getContext()));
            }
        }
        pipelineDefinition.removeAll(toRemove);
        pipelineDefinition.addAll(toAdd);
        // Assemble the output.
        final Collection<Statement> result
                = new ArrayList<>(pipelineRdf.size());
        result.addAll(pipelineDefinition);
        for (Resource configuration : configurations) {
            result.addAll(
                    graphs.getOrDefault(configuration, Collections.EMPTY_LIST));
        }
        return result;
    }


}
