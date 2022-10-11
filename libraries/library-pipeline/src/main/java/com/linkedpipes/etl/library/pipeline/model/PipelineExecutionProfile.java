package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.Resource;

public record PipelineExecutionProfile(
        /*
         * Resource for the execution profile.
         */
        Resource resource,
        /*
         * How should RDF stores be created.
         * TODO: Replace with enum.
         */
        Resource rdfRepositoryPolicy,
        /*
         * Type of RDF store to be by data units.
         * TODO: Replace with enum.
         */
        Resource rdfRepositoryType,
        /*
         * Retention policy for execution logs. Apply only to full execution,
         * can be overloaded.
         */
        DataRetentionPolicy logRetentionPolicy,
        /*
         * Retention policy for debug data. Apply only to full execution,
         * can be overloaded.
         */
        DataRetentionPolicy debugDataRetentionPolicy,
        /*
         * If set limit the number of failed pipeline
         * executions stored. Apply only to full execution.
         */
        Integer failedExecutionLimit,
        /*
         * Can be null, if set limit the number of successful pipeline
         * executions stored. Apply only to full execution.
         */
        Integer successfulExecutionLimit
) {

}
