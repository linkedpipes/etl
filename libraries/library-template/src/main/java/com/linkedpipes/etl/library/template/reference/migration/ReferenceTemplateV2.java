package com.linkedpipes.etl.library.template.reference.migration;

import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;

class ReferenceTemplateV2 {

    /**
     * Remove configuration description and reference to it and delete the
     * configuration description file.
     * <p>
     * As this is solved by ignoring the data using the raw instance, there
     * is effectively no change here.
     */
    public void migrateToV3(RawReferenceTemplate template) {
        template.version = 3;
    }

}
