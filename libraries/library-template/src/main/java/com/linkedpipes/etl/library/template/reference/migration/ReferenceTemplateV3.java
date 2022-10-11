package com.linkedpipes.etl.library.template.reference.migration;

import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

class ReferenceTemplateV3 {

    /**
     * In this version an explicit reference to the configuration graph
     * was added. So we just set it to default value no matter the previous
     * state.
     */
    public void migrateToV4(RawReferenceTemplate template) {
        String resourceAsString = template.resource.stringValue();
        Resource configurationGraph = SimpleValueFactory.getInstance()
                .createIRI(resourceAsString + "/configuration");
        template.version = 4;
        template.configurationGraph = configurationGraph;
    }

}
