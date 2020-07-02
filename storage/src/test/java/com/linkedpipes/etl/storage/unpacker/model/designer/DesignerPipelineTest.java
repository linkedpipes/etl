package com.linkedpipes.etl.storage.unpacker.model.designer;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.ClosableRdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.storage.unpacker.model.ModelLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class DesignerPipelineTest {

    @Test
    public void load_00() throws Exception {
        DesignerPipeline pipeline = loadPipeline("unpacker/designer/00.trig");

        Assert.assertEquals("http://localhost/pipeline", pipeline.getIri());
        Assert.assertEquals("Empty pipeline", pipeline.getLabel());
        Assert.assertEquals(1, pipeline.getVersion());
        Assert.assertEquals(
                "http://linkedpipes.com/ontology/repository/SingleRepository",
                pipeline.getExecutionProfile().getRdfRepositoryPolicy());

        Assert.assertTrue(pipeline.getComponents().isEmpty());
        Assert.assertTrue(pipeline.getConnections().isEmpty());

        // Test get on missing component.

        Assert.assertNull(pipeline.getComponent(
                "http://localhost/pipeline/3405c1ee"));
    }

    @Test
    public void load_01() throws Exception {
        DesignerPipeline pipeline = loadPipeline("unpacker/designer/01.trig");

        Assert.assertEquals("http://localhost/pipeline", pipeline.getIri());
        Assert.assertEquals("TextHolder", pipeline.getLabel());
        Assert.assertEquals(1, pipeline.getVersion());

        Assert.assertEquals(1, pipeline.getComponents().size());
        Assert.assertTrue(pipeline.getConnections().isEmpty());

        DesignerComponent component = pipeline.getComponents().get(0);
        Assert.assertEquals(
                "http://localhost/pipeline/3405c1ee",
                component.getIri());
        Assert.assertEquals(
                "http://etl.linkedpipes.com/resources/components/1476115743178/0.0.0",
                component.getTemplate());
        Assert.assertEquals(
                "http://localhost/pipeline/3405c1ee/configuration",
                component.getConfigurationGraphs().get(0));
        Assert.assertEquals(1, component.getTypes().size());
        Assert.assertEquals(
                "http://linkedpipes.com/ontology/Component",
                component.getTypes().get(0));

        // Test get on missing component.

        Assert.assertNull(pipeline.getComponent(
                "http://localhost/pipeline/nonexisting"));
    }

    @Test
    public void load_02() throws Exception {
        DesignerPipeline pipeline = loadPipeline("unpacker/designer/02.trig");

        Assert.assertEquals("http://localhost/pipeline", pipeline.getIri());
        Assert.assertEquals("TextHolder and FilesToRdf", pipeline.getLabel());
        Assert.assertEquals(1, pipeline.getVersion());

        Assert.assertEquals(2, pipeline.getComponents().size());
        Assert.assertEquals(1, pipeline.getConnections().size());

        DesignerComponent textHolder = pipeline.getComponent(
                "http://localhost/pipeline/9d21ebd5");
        Assert.assertEquals(
                "http://localhost/pipeline/9d21ebd5",
                textHolder.getIri());
        Assert.assertEquals(
                "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0",
                textHolder.getTemplate());
        Assert.assertEquals(
                "http://localhost/pipeline/9d21ebd5/configuration",
                textHolder.getConfigurationGraphs().get(0));

        DesignerComponent filesToRdf = pipeline.getComponent(
                "http://localhost/pipeline/b67542e2");
        Assert.assertEquals(
                "http://localhost/pipeline/b67542e2",
                filesToRdf.getIri());
        Assert.assertEquals(
                "http://etl.linkedpipes.com/resources/components/t-filesToRdfGraph/0.0.0",
                filesToRdf.getTemplate());
        Assert.assertEquals(
                "http://localhost/pipeline/b67542e2/configuration",
                filesToRdf.getConfigurationGraphs().get(0));

        DesignerConnection connection = pipeline.getConnections().get(0);
        Assert.assertEquals(
                "http://localhost/pipeline/connection/dff7b779",
                connection.getIri());
        Assert.assertEquals("FilesOutput", connection.getSourceBinding());
        Assert.assertEquals("http://localhost/pipeline/9d21ebd5",
                connection.getSourceComponent());
        Assert.assertEquals("InputFiles", connection.getTargetBinding());
        Assert.assertEquals("http://localhost/pipeline/b67542e2",
                connection.getTargetComponent());
    }

    private DesignerPipeline loadPipeline(String resourceName)
            throws IOException, RdfUtilsException {
        ClosableRdfSource source = Rdf4jUtils.loadAsSource(resourceName);
        DesignerPipeline pipeline = ModelLoader.loadDesignerPipeline(source);
        source.close();
        return pipeline;
    }

}

