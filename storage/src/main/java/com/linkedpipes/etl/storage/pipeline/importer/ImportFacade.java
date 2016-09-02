package com.linkedpipes.etl.storage.pipeline.importer;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.mapping.Mapping;
import com.linkedpipes.etl.storage.mapping.MappingFacade;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Update resources in pipeline. Can be used to import pipelines.
 *
 * @author Petr Škoda
 */
@Service
public class ImportFacade {

    private static final Logger LOG
            = LoggerFactory.getLogger(ImportFacade.class);

    @Autowired
    private TemplateFacade templateFacade;

    @Autowired
    private MappingFacade mappingFacade;

    public interface Options {

        /**
         * Pipeline IRI. If set pipeline resources are updated.
         *
         * @return
         */
        public IRI getPipelineIri();

    }

    /**
     * Given a pipeline return imported version. The new version may share
     * statements with given pipeline.
     *
     * If there are references to new templates the new templates are created
     * and removed from the pipeline definition.
     *
     * @return
     */
    public Collection<Statement> update(Collection<Statement> pipelineRdf,
            Options options) {
        pipelineRdf = updateTemplates(pipelineRdf);

        if (options.getPipelineIri() != null) {
            pipelineRdf = updateResources(pipelineRdf,
                    options.getPipelineIri().stringValue());
        }
        return pipelineRdf;
    }

    /**
     * Check that all templates are known. If not try to import them and
     * update the reference.
     *
     * @param pipelineRdf
     * @return
     */
    private Collection<Statement> updateTemplates(
            Collection<Statement> pipelineRdf) {
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

    /**
     * Change resource IRI for pipeline resources.
     *
     * @param pipelineRdf
     * @param baseIri
     * @return
     */
    private static Collection<Statement> updateResources(
            Collection<Statement> pipelineRdf, String baseIri) {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final Map<Resource, Resource> mapping = new HashMap<>();
        for (Statement s : pipelineRdf) {
            if (s.getPredicate().equals(RDF.TYPE) &&
                    !mapping.containsKey(s.getSubject())) {
                if (s.getObject().equals(Pipeline.TYPE)) {
                    // For pipeline we the IRI as it is.
                    mapping.put(s.getSubject(), valueFactory.createIRI(
                            baseIri));
                } else {
                    mapping.put(s.getSubject(), valueFactory.createIRI(
                            baseIri + "/" + (mapping.size() + 1)));
                }
            }
        }
        final List<Statement> result = new ArrayList<>(pipelineRdf.size());
        for (Statement s : pipelineRdf) {
            final Resource context = mapping.getOrDefault(
                    s.getContext(), s.getContext());
            if (mapping.containsKey(s.getSubject())) {
                if (mapping.containsKey(s.getObject())) {
                    result.add(valueFactory.createStatement(
                            mapping.get(s.getSubject()), s.getPredicate(),
                            mapping.get(s.getObject()), context));
                } else {
                    result.add(valueFactory.createStatement(
                            mapping.get(s.getSubject()), s.getPredicate(),
                            s.getObject(), context));
                }
            } else {
                if (mapping.containsKey(s.getObject())) {
                    result.add(valueFactory.createStatement(
                            s.getSubject(), s.getPredicate(),
                            mapping.get(s.getObject()), context));
                } else {
                    result.add(valueFactory.createStatement(
                            s.getSubject(), s.getPredicate(), s.getObject(),
                            context));
                }
            }
        }
        return result;
    }

}
