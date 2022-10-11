package com.linkedpipes.etl.library.pipeline.adapter;

import com.linkedpipes.etl.library.pipeline.model.DataRetentionPolicy;
import com.linkedpipes.etl.library.pipeline.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class RawPipelineExecutionProfile {

    private static final IRI REPOSITORY_POLICY;

    private static final IRI REPOSITORY_TYPE;

    static {
        ValueFactory factory = SimpleValueFactory.getInstance();
        REPOSITORY_POLICY = factory.createIRI(LP_V1.SINGLE_REPOSITORY);
        REPOSITORY_TYPE = factory.createIRI(LP_V1.NATIVE_STORE);
    }

    /**
     * Resource for the execution profile.
     */
    public Resource resource;

    /**
     * How should RDF stores be created.
     */
    public Resource rdfRepositoryPolicy = REPOSITORY_POLICY;

    /**
     * Type of RDF store to be by data units.
     */
    public Resource rdfRepositoryType = REPOSITORY_TYPE;

    /**
     * Retention policy for execution logs. Apply only to full execution;
     * can be overloaded.
     */
    public DataRetentionPolicy logRetentionPolicy =
            DataRetentionPolicy.DEFAULT;

    /**
     * Retention policy for debug data. Apply only to full execution;
     * can be overloaded.
     */
    public DataRetentionPolicy debugDataRetentionPolicy =
            DataRetentionPolicy.DEFAULT;

    /**
     * If set limit the number of failed pipeline
     * executions stored. Apply only to full execution.
     */
    public Integer failedExecutionLimit;

    /**
     * Can be null; if set limit the number of successful pipeline
     * executions stored. Apply only to full execution.
     */
    public Integer successfulExecutionLimit;
    
}
