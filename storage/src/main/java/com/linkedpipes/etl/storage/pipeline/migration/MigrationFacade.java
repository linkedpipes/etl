package com.linkedpipes.etl.storage.pipeline.migration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.rdf.StatementsCollection;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Migrate pipeline from older version to current version.
 *
 * @author Petr Škoda
 */
@Service
public class MigrationFacade {

    private static final Logger LOG
            = LoggerFactory.getLogger(MigrationFacade.class);

    public static final IRI COMPONENT;

    public static final IRI HAS_TEMPLATE;

    public static class MigrationFailed extends BaseException {

        public MigrationFailed(String message, Object... args) {
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

    @Autowired
    private TemplateFacade templateFacade;

    /**
     * Return copy of given pipeline migrated to current version. The new
     * pipeline can use statements from the old pipeline.
     *
     * @param pipelineRdf
     * @return
     */
    public Collection<Statement> migrate(Collection<Statement> pipelineRdf)
            throws MigrationFailed {
        // Split pipeline into graphs and locate pipeline resource.
        final Resource pplResource = RdfUtils.find(pipelineRdf,
                Pipeline.TYPE);
        if (pipelineRdf == null) {
            throw new MigrationFailed("Missing pipeline resource.");
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
                migrateFrom_0(pplObject);
            case 1: // Current version.
                break;
            default:
                throw new MigrationFailed(
                        "Invalid version ({}) for pipeline: {}",
                        version, pplResource);
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
     */
    private void migrateFrom_0(RdfObjects pipeline) {
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

}
