package com.linkedpipes.etl.library.template.reference.migration;

import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;

class ReferenceTemplateV0 {

    /**
     * There were no changes between 0 and 1.
     */
    public void migrateToV1(RawReferenceTemplate template) {
        template.version = 1;
    }

}
