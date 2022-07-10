package com.linkedpipes.etl.library.pipeline.model;

import com.linkedpipes.etl.library.pipeline.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public record PipelineExecutionProfile(
        /*
         * Resource for the execution profile.
         */
        Resource resource,
        /*
         * How should RDF stores be created.
         */
        Resource rdfRepositoryPolicy,
        /*
         * Type of RDF store to be by data units.
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

    private static final IRI DEFAULT_POLICY;

    private static final IRI DEFAULT_TYPE;

    static {
        ValueFactory factory = SimpleValueFactory.getInstance();
        DEFAULT_POLICY = factory.createIRI(LP_V1.SINGLE_REPOSITORY);
        DEFAULT_TYPE = factory.createIRI(LP_V1.NATIVE_STORE);
    }

    public PipelineExecutionProfile(Resource resource) {
        this(
                resource,
                DEFAULT_POLICY,
                DEFAULT_TYPE,
                DataRetentionPolicy.DEFAULT,
                DataRetentionPolicy.DEFAULT,
                null,
                null
        );
    }

}
