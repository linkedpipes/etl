package com.linkedpipes.etl.library.template.reference.migration;

import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

class ReferenceTemplateV3 {

    /**
     * In this version an explicit reference to the configuration graph
     * was added. So we just set it to default value no matter the previous
     * state.
     */
    public ReferenceTemplate migrateToV4(ReferenceTemplate template) {
        String resourceAsString = template.resource().stringValue();
        Resource configurationGraph = SimpleValueFactory.getInstance()
                .createIRI(resourceAsString + "/configuration");
        return new ReferenceTemplate(
                template.resource(),
                template.template(),
                template.label(),
                template.description(),
                template.note(),
                template.color(),
                template.tags(),
                template.knownAs(),
                template.pluginTemplate(),
                4,
                template.configuration(),
                configurationGraph);
    }

}
