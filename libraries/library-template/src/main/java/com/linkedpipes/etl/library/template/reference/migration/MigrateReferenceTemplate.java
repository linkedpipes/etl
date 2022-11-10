package com.linkedpipes.etl.library.template.reference.migration;


import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;

import java.util.Map;

public class MigrateReferenceTemplate {

    /**
     * For a given reference template provides URL of a plugin template.
     */
    private final Map<Resource, Resource> templateToPlugin;

    public MigrateReferenceTemplate(Map<Resource, Resource> templateToPlugin) {
        this.templateToPlugin = templateToPlugin;
    }

    /**
     * Does not change given template.
     */
    public ReferenceTemplate migrate(RawReferenceTemplate template)
            throws ReferenceMigrationFailed {
        RawReferenceTemplate working = new RawReferenceTemplate(template);
        int initialVersion = template.version;
        if (initialVersion < 1) {
            (new ReferenceTemplateV0()).migrateToV1(working);
        }
        if (initialVersion < 2) {
            (new ReferenceTemplateV1(templateToPlugin)).migrateToV2(working);
        }
        if (initialVersion < 3) {
            (new ReferenceTemplateV2()).migrateToV3(working);
        }
        if (initialVersion < 4) {
            (new ReferenceTemplateV3()).migrateToV4(working);
        }
        if (initialVersion < 5) {
            (new ReferenceTemplateV4(templateToPlugin)).migrateToV5(working);
        }
        return working.toReferenceTemplate();
    }

}
