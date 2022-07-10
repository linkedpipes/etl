package com.linkedpipes.etl.library.template.reference.migration;


import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;

class ReferenceTemplateV2 {

    /**
     * Remove configuration description and reference to it and delete the
     * configuration description file.
     * <p>
     * We do not need to do any of this on this level.
     */
    public ReferenceTemplate migrateToV3(ReferenceTemplate template) {
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
                3,
                template.configuration(),
                template.configurationGraph());
    }

}
