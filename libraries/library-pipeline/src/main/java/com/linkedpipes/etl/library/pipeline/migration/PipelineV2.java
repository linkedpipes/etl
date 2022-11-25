package com.linkedpipes.etl.library.pipeline.migration;

import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import org.eclipse.rdf4j.model.IRI;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Reason for version 5 was to unify versioning for templates and pipelines.
 */
public class PipelineV2 {

    public void migrateToV5(RawPipeline pipeline) {
        pipeline.version = 5;
        pipeline.lastUpdate = LocalDateTime.now();
        if (pipeline.resource instanceof IRI iri) {
            pipeline.created = guessCreateTime(iri);
        }
    }

    private LocalDateTime guessCreateTime(IRI resource) {
        //  For some time a prefix was used.
        String name = resource.getLocalName()
                .replace("created-", "");
        long timestamp;
        try {
            timestamp = Long.parseLong(name);
        } catch (NumberFormatException ex) {
            return null;
        }
        Date date = new Date(timestamp);
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}
