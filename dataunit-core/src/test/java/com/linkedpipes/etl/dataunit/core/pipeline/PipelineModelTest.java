package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.dataunit.core.Rdf4jSource;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PipelineModelTest {

    @Test
    public void withRepository() throws Exception {
        Rdf4jSource source = new Rdf4jSource();
        source.loadTestResource("withRepository.ttl");
        PipelineModel pipelineModel = new PipelineModel();
        pipelineModel.load("http://pipeline", null, source);

        Assert.assertEquals("http://repositoryPolicy",
                pipelineModel.getRdfRepositoryPolicy());
        Assert.assertEquals("http://repositoryType",
                pipelineModel.getRdfRepositoryType());
        Assert.assertEquals("http://repository",
                pipelineModel.getRdfRepository());
    }

    @Test
    public void defaultRepositoryValuesNoRepositoryObject() throws Exception {
        Rdf4jSource source = new Rdf4jSource();
        source.loadTestResource("pipelineResource.ttl");
        PipelineModel pipelineModel = new PipelineModel();
        pipelineModel.load("http://pipeline", null, source);

        Assert.assertEquals(LP_PIPELINE.SINGLE_REPOSITORY,
                pipelineModel.getRdfRepositoryPolicy());
        Assert.assertEquals(LP_PIPELINE.NATIVE_STORE,
                pipelineModel.getRdfRepositoryType());
        Assert.assertNull(pipelineModel.getRdfRepository());
    }

    @Test
    public void dataUnitGroups() throws Exception {
        Rdf4jSource source = new Rdf4jSource();
        source.loadTestResource("twoConnected.ttl");
        PipelineModel pipelineModel = new PipelineModel();
        pipelineModel.load("http://pipeline", null, source);

        List<String> actual = pipelineModel.getSourcesFor("http://comp/t/1");
        List<String> expected = Arrays.asList(
                "http://comp/s1/1", "http://comp/s2/1");

        Assert.assertArrayEquals(actual.toArray(), expected.toArray());
    }

}
