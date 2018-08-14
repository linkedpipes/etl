package com.linkedpipes.etl.storage.migration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.pipeline.transformation.TransformationFailed;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.List;

/**
 * Change core templates from local host to etl.linkedpipes.com.
 *
 * Example of conversion:
 * http://localhost:8080/resources/components/t-tabular
 * http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0
 */
public class MigrateV0ToV1 {

    private static final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    private static final IRI COMPONENT;

    private static final IRI HAS_TEMPLATE;

    static {
        COMPONENT = valueFactory.createIRI(LP_PIPELINE.COMPONENT);
        HAS_TEMPLATE = valueFactory.createIRI(LP_PIPELINE.HAS_TEMPLATE);
    }

    private final TemplateFacade templateFacade;

    private final boolean throwOnMissing;

    public MigrateV0ToV1(TemplateFacade templateFacade,
            boolean throwOnMissing) {
        this.templateFacade = templateFacade;
        this.throwOnMissing = throwOnMissing;
    }

    public void pipeline(RdfObjects pipeline) throws TransformationFailed {
        for (RdfObjects.Entity component : pipeline.getTyped(COMPONENT)) {
            migrateComponent(component);
        }
    }

    private void migrateComponent(RdfObjects.Entity component)
            throws TransformationFailed {
        RdfObjects.Entity template = getTemplate(component);
        String name = templateName(template);
        IRI newTemplateIri = searchMatchingTemplateByName(name);
        if (newTemplateIri == null) {
            if (this.throwOnMissing) {
                throw new TransformationFailed(
                        "Can not convert '{}' for '{}'",
                        name, component.getResource());
            }
            return;
        }
        component.deleteReferences(HAS_TEMPLATE);
        component.add(HAS_TEMPLATE, newTemplateIri);
    }

    private RdfObjects.Entity getTemplate(RdfObjects.Entity component)
            throws TransformationFailed {
        List<RdfObjects.Entity> templates =
                component.getReferences(HAS_TEMPLATE);
        if (templates.size() != 1) {
            throw new TransformationFailed(
                    "Invalid number of templates: {}",
                    component.getResource());
        }
        return templates.get(0);
    }

    private String templateName(RdfObjects.Entity template) {
        // The extracted name is /t-tabular and we add / to the end
        // to prevent t-tabular to match t-tabularUv, also every name
        // is followed by /{version}.
        String iri = template.getResource().stringValue();
        return iri.substring(iri.lastIndexOf("/")) + "/";
    }

    private IRI searchMatchingTemplateByName(String name) {
        for (Template template : templateFacade.getTemplates()) {
            if (template.getIri().contains(name)) {
                return valueFactory.createIRI(template.getIri());
            }
        }
        return null;
    }

}
