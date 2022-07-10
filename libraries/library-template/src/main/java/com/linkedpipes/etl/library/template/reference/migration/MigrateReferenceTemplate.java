package com.linkedpipes.etl.library.template.reference.migration;


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

    public ReferenceTemplate migrate(ReferenceTemplate template)
            throws MigrationFailed {
        ReferenceTemplate result = template;
        int initialVersion = template.version();
        if (initialVersion < 1) {
            result = (new ReferenceTemplateV0())
                    .migrateToV1(result);
        }
        if (initialVersion < 2) {
            result = (new ReferenceTemplateV1(templateToPlugin))
                    .migrateToV2(result);
        }
        if (initialVersion < 3) {
            result = (new ReferenceTemplateV2())
                    .migrateToV3(result);
        }
        if (initialVersion < 4) {
            result = (new ReferenceTemplateV3())
                    .migrateToV4(result);
        }
        if (initialVersion < 5) {
            result = (new ReferenceTemplateV4(templateToPlugin))
                    .migrateToV5(result);
        }
        return result;
    }

}
