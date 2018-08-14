package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.repository.WritableTemplateRepository;
import org.eclipse.rdf4j.model.Statement;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Remove configuration description and reference to it for non JAR templates.
 */
class TemplateV2ToV3 {

    private final WritableTemplateRepository repository;

    public TemplateV2ToV3(WritableTemplateRepository repository) {
        this.repository = repository;
    }

    public void migrate(Template template) throws BaseException {
        if (!Template.Type.REFERENCE_TEMPLATE.equals(template.getType())) {
            return;
        }
        this.deleteConfigDescription(template);
        this.updateDefinition(template);
        this.updateInterface(template);
    }

    private void deleteConfigDescription(Template template)
            throws BaseException {
        File path = new File(
                this.repository.getDirectory(template),
                "configuration-description.trig");
        if (path.exists()) {
            if (!path.delete()) {
                throw new BaseException(
                        "Can't delete deprecated template " +
                                "configuration description: {}",
                        path);
            }
        }
    }

    private void updateDefinition(Template template)
            throws RdfUtils.RdfException {
        Collection<Statement> statements =
                this.repository.getDefinition(template);
        this.repository.setDefinition(template,
                this.removeConfigDescriptionReference(statements));
    }

    private List<Statement> removeConfigDescriptionReference(
            Collection<Statement> statements) {
        return statements.stream().filter(
                (st) -> {
                    String predicate = st.getPredicate().stringValue();
                    return !LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION
                            .equals(predicate);
                }
        ).collect(Collectors.toList());
    }

    private void updateInterface(Template template)
            throws RdfUtils.RdfException {
        Collection<Statement> statements =
                this.repository.getInterface(template);
        this.repository.setInterface(template,
                this.removeConfigDescriptionReference(statements));
    }


}
