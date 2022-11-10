package com.linkedpipes.etl.library.pipeline.adapter;

import com.linkedpipes.etl.library.pipeline.model.DataRetentionPolicy;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.library.pipeline.model.PipelineDataFlow;
import com.linkedpipes.etl.library.pipeline.model.PipelineExecutionProfile;
import com.linkedpipes.etl.library.pipeline.model.PipelineVertex;
import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipelineAdapterTest {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void pipelineToRdfAndBack() {
        Pipeline expected = new Pipeline(
                valueFactory.createIRI("http://pipeline"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Label",
                5,
                "Note",
                List.of("First", "Second"),
                new PipelineExecutionProfile(
                        valueFactory.createIRI(
                                "http://pipeline/profile/default"),
                        valueFactory.createIRI("http://policy"),
                        valueFactory.createIRI("http://type"),
                        DataRetentionPolicy.DEFAULT,
                        DataRetentionPolicy.DEFAULT,
                        1, 3),
                List.of(
                        new PipelineComponent(
                                valueFactory.createIRI("http://component/1"),
                                "First", "Description", "First Note",
                                "red", 10, 20,
                                valueFactory.createIRI("http://template"),
                                false,
                                null,
                                valueFactory.createIRI(
                                        "http://first/configuration")),
                        new PipelineComponent(
                                valueFactory.createIRI("http://component/2"),
                                "First", "Description", "First Note",
                                "red", 10, 20,
                                valueFactory.createIRI("http://template"),
                                false,
                                null,
                                valueFactory.createIRI(
                                        "http://first/configuration"))),
                List.of(new PipelineDataFlow(
                        valueFactory.createIRI("http://flow/1"),
                        valueFactory.createIRI("http://component/1"),
                        valueFactory.createLiteral("source"),
                        valueFactory.createIRI("http://component/2"),
                        valueFactory.createLiteral("target"),
                        List.of(new PipelineVertex(
                                valueFactory.createIRI("http://vertex/1"),
                                10, 10, 0)))),
                List.of());
        Statements statements = PipelineToRdf.asRdf(expected);
        List<RawPipeline> candidates =
                RdfToRawPipeline.asRawPipelines(statements);
        Assertions.assertEquals(1, candidates.size());
        // As using equals does not check content we need to get
        // rid of configuration.
        RawPipeline candidate = candidates.get(0);
        for (RawPipelineComponent component : candidate.components) {
            component.configuration = null;
        }
        Pipeline actual = candidate.toPipeline();

        Assertions.assertEquals(expected, actual);
    }

}

