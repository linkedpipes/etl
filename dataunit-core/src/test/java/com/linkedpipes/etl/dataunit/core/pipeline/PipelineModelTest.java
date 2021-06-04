package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.dataunit.core.Rdf4jSource;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class PipelineModelTest {

    @Test
    public void withRepository() throws Exception {
        Rdf4jSource source = new Rdf4jSource();
        source.loadTestResource("withRepository.ttl");
        PipelineModel pipelineModel = new PipelineModel();
        pipelineModel.load("http://pipeline", source);

        Assertions.assertEquals("http://repositoryPolicy",
                pipelineModel.getRdfRepositoryPolicy());
        Assertions.assertEquals("http://repositoryType",
                pipelineModel.getRdfRepositoryType());
        Assertions.assertEquals("http://repository",
                pipelineModel.getRdfRepository());
    }

    @Test
    public void defaultRepositoryValuesNoRepositoryObject() throws Exception {
        Rdf4jSource source = new Rdf4jSource();
        source.loadTestResource("pipelineResource.ttl");
        PipelineModel pipelineModel = new PipelineModel();
        pipelineModel.load("http://pipeline", source);

        Assertions.assertEquals(LP_PIPELINE.SINGLE_REPOSITORY,
                pipelineModel.getRdfRepositoryPolicy());
        Assertions.assertEquals(LP_PIPELINE.NATIVE_STORE,
                pipelineModel.getRdfRepositoryType());
        Assertions.assertNull(pipelineModel.getRdfRepository());
    }

    @Test
    public void dataUnitGroups() throws Exception {
        Rdf4jSource source = new Rdf4jSource();
        source.loadTestResource("twoConnected.ttl");
        PipelineModel pipelineModel = new PipelineModel();
        pipelineModel.load("http://pipeline", source);

        List<String> actual = pipelineModel.getSourcesFor("http://comp/t/1");
        List<String> expected = Arrays.asList(
                "http://comp/s1/1", "http://comp/s2/1");

        Assertions.assertArrayEquals(actual.toArray(), expected.toArray());
    }

}
