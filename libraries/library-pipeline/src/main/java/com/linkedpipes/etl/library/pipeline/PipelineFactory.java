package com.linkedpipes.etl.library.pipeline;

import com.linkedpipes.etl.library.pipeline.model.DataRetentionPolicy;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.model.PipelineExecutionProfile;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.time.LocalDateTime;
import java.util.Collections;

public class PipelineFactory {

    public static final String SINGLE_REPOSITORY =
            "http://linkedpipes.com/ontology/repository/SingleRepository";

    public static final String NATIVE_STORE =
            "http://linkedpipes.com/ontologyrepository/NativeStore";

    public static Pipeline createEmpty(Resource resource, String label) {
        LocalDateTime now = LocalDateTime.now();
        return new Pipeline(
                resource, now, now, label, Pipeline.VERSION,
                null, Collections.emptyList(),
                createProfile(resource),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
    }

    private static PipelineExecutionProfile createProfile(Resource pipeline) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        return new PipelineExecutionProfile(
                PipelineExecutionProfile.createResource(pipeline),
                valueFactory.createIRI(SINGLE_REPOSITORY),
                valueFactory.createIRI(NATIVE_STORE),
                DataRetentionPolicy.DEFAULT,
                DataRetentionPolicy.DEFAULT,
                null, null);
    }

}
