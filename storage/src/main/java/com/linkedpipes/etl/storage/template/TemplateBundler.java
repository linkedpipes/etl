package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.template.plugin.adapter.PluginTemplateToRdf;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.ReferenceTemplateToRdf;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import com.linkedpipes.etl.storage.StorageException;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

/**
 * Embeds every template referenced by a Designer pipeline (definition,
 * default configuration, configuration description; following
 * reference-template chains to their underlying plugin template) into the
 * pipeline's own RDF. The result is self-contained: executor can unpack and
 * run it without any further calls back to storage.
 */
public class TemplateBundler {

    private final TemplateFacade templateFacade;

    public TemplateBundler(TemplateFacade templateFacade) {
        this.templateFacade = templateFacade;
    }

    public Statements bundle(Statements pipelineRdf) throws StorageException {
        StatementsBuilder result = Statements.arrayList().builder();
        result.addAll(pipelineRdf);
        Set<Resource> visited = new HashSet<>();
        for (Resource templateIri : findReferencedTemplates(pipelineRdf)) {
            embedTemplate(result, templateIri, visited);
        }
        return result;
    }

    private Set<Resource> findReferencedTemplates(Statements pipelineRdf) {
        Set<Resource> result = new HashSet<>();
        for (Statement statement : pipelineRdf) {
            if (!LP_V1.HAS_TEMPLATE.equals(statement.getPredicate().stringValue())) {
                continue;
            }
            Value object = statement.getObject();
            if (object instanceof Resource resource) {
                result.add(resource);
            }
        }
        return result;
    }

    private void embedTemplate(StatementsBuilder target, Resource templateIri, Set<Resource> visited)
            throws StorageException {
        if (!visited.add(templateIri)) {
            return;
        }
        if (templateFacade.isPluginTemplate(templateIri)) {
            embedPluginTemplate(target, templateIri);
        } else {
            embedReferenceTemplate(target, templateIri, visited);
        }
    }

    private void embedPluginTemplate(StatementsBuilder target, Resource templateIri) throws StorageException {
        PluginTemplate template = templateFacade.getPluginTemplate(templateIri);
        if (template == null) {
            throw new StorageException("Missing plugin template '{}'.", templateIri);
        }
        target.addAll(PluginTemplateToRdf.asRdf(template));
    }

    private void embedReferenceTemplate(StatementsBuilder target, Resource templateIri, Set<Resource> visited)
            throws StorageException {
        ReferenceTemplate template = templateFacade.getReferenceTemplate(templateIri);
        if (template == null) {
            throw new StorageException("Missing reference template '{}'.", templateIri);
        }
        target.addAll(ReferenceTemplateToRdf.definitionAsRdf(template));
        target.addAll(ReferenceTemplateToRdf.configurationAsRdf(template));
        embedTemplate(target, template.plugin(), visited);
    }
}
