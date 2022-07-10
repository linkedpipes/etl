package com.linkedpipes.etl.library.template.reference.migration;


import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;

class ReferenceTemplateV0 {

    /**
     * There were no changes between 0 and 1 from the perspective of
     * templates.
     */
    public ReferenceTemplate migrateToV1(ReferenceTemplate template) {
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
                1,
                template.configuration(),
                template.configurationGraph());
    }

}
