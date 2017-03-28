package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.pipeline.model.Component;
import com.linkedpipes.etl.executor.pipeline.model.Connection;
import com.linkedpipes.etl.executor.pipeline.model.ExecutionType;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class PipelineTest {

    @Test
    public void loadPipelineModel() throws Exception {
        final File directory =
                Files.createTempDirectory("lp-test").toFile();
        final File file = new File(Thread.currentThread()
                .getContextClassLoader()
                .getResource("pipeline/twoConnectedComponents.trig")
                .getPath());
        //
        final Pipeline pipeline = new Pipeline();
        pipeline.load(file, directory);
        //
        Assert.assertEquals("http://pipeline", pipeline.getPipelineIri());
        Assert.assertEquals("http://pipeline/graph",
                pipeline.getPipelineGraph());
        final PipelineModel model = pipeline.getModel();
        Assert.assertEquals(2, model.getComponents().size());
        Assert.assertEquals(1, model.getConnections().size());
        //
        final Component component1 =
                model.getComponents().get(0);
        Assert.assertEquals("http://pipeline/component/1",
                component1.getIri());
        Assert.assertEquals("component_1_path",
                component1.getJarPath());
        Assert.assertEquals(ExecutionType.EXECUTE,
                component1.getExecutionType());
        Assert.assertEquals(2,
                component1.getConfigurations().size());
        Assert.assertEquals("http://pipeline/configuration/1/1",
                component1.getConfigurations().get(0).getGraph());
        Assert.assertEquals("http://pipeline/configuration/1/2",
                component1.getConfigurations().get(1).getGraph());
        Assert.assertEquals(2,
                component1.getPorts().size());
        //
        final Component component2 =
                model.getComponents().get(1);
        Assert.assertEquals("http://pipeline/component/2",
                component2.getIri());
        Assert.assertEquals("component_2_path",
                component2.getJarPath());
        Assert.assertEquals(ExecutionType.EXECUTE,
                component2.getExecutionType());
        Assert.assertEquals(1,
                component2.getConfigurations().size());
        Assert.assertEquals("http://pipeline/configuration/2/1",
                component2.getConfigurations().get(0).getGraph());
        Assert.assertEquals(1,
                component2.getPorts().size());
        //
        final Connection connection =
                model.getConnections().get(0);
        Assert.assertEquals(
                "http://linkedpipes.com/ontology/components/1/dataunit/2",
                connection.getSourceBinding());
        Assert.assertEquals("http://pipeline/component/1",
                connection.getSourceComponent());
        Assert.assertEquals(
                "http://linkedpipes.com/ontology/components/2/dataunit/1",
                connection.getTargetBinding());
        Assert.assertEquals("http://pipeline/component/2",
                connection.getTargetComponent());
        //
        pipeline.closeRepository();
        FileUtils.deleteDirectory(directory);
    }

}
