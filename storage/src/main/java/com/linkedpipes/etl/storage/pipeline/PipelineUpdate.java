package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.mapping.Mapping;
import com.linkedpipes.etl.storage.mapping.MappingFacade;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.rdf.StatementsCollection;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Provide functionality to perform updates on the pipeline.
 *
 * @author Petr Škoda
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
     * @param pipelineRdf
     * @param templateFacade
     * @return Updated pipeline.
     */
    public static Collection<Statement> migrate(
            Collection<Statement> pipelineRdf, TemplateFacade templateFacade)
            throws UpdateFailed {
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
                migrateFrom_0(pplObject, templateFacade);
            case 1: // Current version.
                break;
            default:
                throw new UpdateFailed("Invalid version!");
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
     */
    private static void migrateFrom_0(RdfObjects pipeline, TemplateFacade templateFacade) {
        // Example of conversion:
        // http://localhost:8080/resources/components/t-tabular
        // http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0
        final ValueFactory vf = SimpleValueFactory.getInstance();
        for (RdfObjects.Entity entity : pipeline.getTyped(COMPONENT)) {
            final List<Resource> newTemplates = new LinkedList<>();
            entity.getReferences(HAS_TEMPLATE).forEach((ref) -> {
                String templateIri = ref.getResource().stringValue();
                String name = templateIri.substring(templateIri.lastIndexOf("/") + 1);
                // We need to search for components to match the name.
                for (Template template : templateFacade.getTemplates()) {
                    if (template.getIri().contains(name)) {
                        templateIri = template.getIri();
                        break;
                    }
                }
                newTemplates.add(vf.createIRI(templateIri));
            });
            entity.deleteReferences(HAS_TEMPLATE);
            newTemplates.forEach((e) -> entity.add(HAS_TEMPLATE, e));
        }
    }

    /**
     * Update and return given pipeline based on the given options.
     *
     * @param pipelineRdf Pipeline in the RDF.
     * @param pipelineIri Current IRI of the given pipeline.
     * @param options
     * @return
     * @throws UpdateFailed
     */
    public static Collection<Statement> update(Collection<Statement> pipelineRdf,
            IRI pipelineIri, PipelineOptions options)
            throws UpdateFailed {
        // Update resources.
        if (!pipelineIri.equals(options.getPipelineIri())) {
            pipelineRdf = updateResources(pipelineRdf,
                    options.getPipelineIri().stringValue());
        }
        // Parse pipeline.
        final List<Statement> result = new ArrayList<>(pipelineRdf.size() + 16);
        final List<Statement> pplInstance = new LinkedList<>();
        for (Statement statement : pipelineRdf) {
            if (statement.getSubject().equals(pipelineIri)) {
                pplInstance.add(statement);
            } else {
                result.add(statement);
            }
        }
        // Update labels.
        if (options.getLabels() != null && !options.getLabels().isEmpty()) {
            updateLabels(pplInstance, options.getLabels());
        }

        result.addAll(pplInstance);
        return result;
    }

    /**
     * Perform in-place update of pipeline labels.
     *
     * @param pipelineInstance Statements describing the pipeline.
     * @param labels
     */
    private static void updateLabels(List<Statement> pipelineInstance,
            Collection<Literal> labels) throws UpdateFailed {
        // Remove existing label statements.
        final List<Statement> toRemove = new ArrayList<>(2);
        for (Statement statement : pipelineInstance) {
            if (SKOS.PREF_LABEL.equals(statement.getPredicate())) {
                toRemove.add(statement);
            }
        }
        pipelineInstance.removeAll(toRemove);
        // Add labels.
        final Resource pipelineResource = pipelineInstance.get(0).getSubject();
        final Resource graph = pipelineInstance.get(0).getContext();
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        for (Value value : labels) {
            pipelineInstance.add(valueFactory.createStatement(pipelineResource,
                    SKOS.PREF_LABEL, value, graph));
        }
    }

    /**
     * update resource/graph IRIs.
     *
     * @param pipelineRdf
     * @param baseIri
     * @return
     */
    private static Collection<Statement> updateResources(
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
     * Align templates in the pipeline with local templates. Missing
     * templates are imported.
     *
     * @param pipelineRdf
     * @param templateFacade
     * @param mappingFacade
     * @return
     */
    public static Collection<Statement> updateTemplates(
            Collection<Statement> pipelineRdf, TemplateFacade templateFacade,
            MappingFacade mappingFacade) {
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
            // TODO: Missing pipeline, there is nothing to import.
            return pipelineRdf;
        }
        // Read mappings if there is any.
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
                final String templateIri = mapping.map(templateInfo.getIri());
                // Check if we know the template, if so we can continue.
                if (templateFacade.getTemplate(templateIri) != null) {
                    continue;
                }
                // We import the hierarchy from the top, so for every
                // template to import we require its parent to be
                // already imported.
                final Template parent = templateFacade.getTemplate(mapping.map(
                        templateInfo.getTemplate().stringValue()));
                if (parent == null) {
                    continue;
                }
                // Update the parent reference.
                templateInfo.setTemplate(vf.createIRI(parent.getIri()));
                //
                try {
                    // Create new template from the templateInfo -> import.
                    Template template = templateFacade.createTemplate(
                            templateInfo.getDefinition(),
                            templateInfo.getConfiguration());
                    // Add mapping, so it can be used by other templates.
                    mappingFacade.add(template, mapping.original(
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
                // so we can just ask for mapping. Here we first need to
                // resolve component ot original mapping (if given),
                // then we use mapping to map the original mapping to this
                // instance.
                final String templateIri = mapping.map(mapping.original(
                        statement.getObject().stringValue()));
                toRemove.add(statement);
                toAdd.add(vf.createStatement(statement.getSubject(),
                        statement.getPredicate(),
                        vf.createIRI(templateIri),
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
