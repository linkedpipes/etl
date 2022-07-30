package com.linkedpipes.etl.storage.unpacker.model.designer;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.unpacker.model.ModelLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DesignerPipelineTest {

    @Test
    public void load_00() throws Exception {
        DesignerPipeline pipeline = loadPipeline("unpacker/designer/00.trig");

        Assertions.assertEquals("http://localhost/pipeline", pipeline.getIri());
        Assertions.assertEquals("Empty pipeline", pipeline.getLabel());
        Assertions.assertEquals(1, pipeline.getVersion());
        Assertions.assertEquals(
                "http://linkedpipes.com/ontology/repository/SingleRepository",
                pipeline.getExecutionProfile().getRdfRepositoryPolicy());

        Assertions.assertTrue(pipeline.getComponents().isEmpty());
        Assertions.assertTrue(pipeline.getConnections().isEmpty());

        // Test get on missing component.

        Assertions.assertNull(pipeline.getComponent(
                "http://localhost/pipeline/3405c1ee"));
    }

    @Test
    public void load_01() throws Exception {
        DesignerPipeline pipeline = loadPipeline("unpacker/designer/01.trig");

        Assertions.assertEquals("http://localhost/pipeline", pipeline.getIri());
        Assertions.assertEquals("TextHolder", pipeline.getLabel());
        Assertions.assertEquals(1, pipeline.getVersion());

        Assertions.assertEquals(1, pipeline.getComponents().size());
        Assertions.assertTrue(pipeline.getConnections().isEmpty());

        DesignerComponent component = pipeline.getComponents().get(0);
        Assertions.assertEquals(
                "http://localhost/pipeline/3405c1ee",
                component.getIri());
        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/components/1476115743178/0.0.0",
                component.getTemplate());
        Assertions.assertEquals(
                "http://localhost/pipeline/3405c1ee/configuration",
                component.getConfigurationGraphs().get(0));
        Assertions.assertEquals(1, component.getTypes().size());
        Assertions.assertEquals(
                "http://linkedpipes.com/ontology/Component",
                component.getTypes().get(0));

        // Test get on missing component.

        Assertions.assertNull(pipeline.getComponent(
                "http://localhost/pipeline/nonexisting"));
    }

    @Test
    public void load_02() throws Exception {
        DesignerPipeline pipeline = loadPipeline("unpacker/designer/02.trig");

        Assertions.assertEquals(
                "http://localhost/pipeline", pipeline.getIri());
        Assertions.assertEquals(
                "TextHolder and FilesToRdf", pipeline.getLabel());
        Assertions.assertEquals(1, pipeline.getVersion());

        Assertions.assertEquals(2, pipeline.getComponents().size());
        Assertions.assertEquals(1, pipeline.getConnections().size());

        DesignerComponent textHolder = pipeline.getComponent(
                "http://localhost/pipeline/9d21ebd5");
        Assertions.assertEquals(
                "http://localhost/pipeline/9d21ebd5",
                textHolder.getIri());
        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0",
                textHolder.getTemplate());
        Assertions.assertEquals(
                "http://localhost/pipeline/9d21ebd5/configuration",
                textHolder.getConfigurationGraphs().get(0));

        DesignerComponent filesToRdf = pipeline.getComponent(
                "http://localhost/pipeline/b67542e2");
        Assertions.assertEquals(
                "http://localhost/pipeline/b67542e2",
                filesToRdf.getIri());
        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/components/t-filesToRdfGraph/0.0.0",
                filesToRdf.getTemplate());
        Assertions.assertEquals(
                "http://localhost/pipeline/b67542e2/configuration",
                filesToRdf.getConfigurationGraphs().get(0));

        DesignerConnection connection = pipeline.getConnections().get(0);
        Assertions.assertEquals(
                "http://localhost/pipeline/connection/dff7b779",
                connection.getIri());
        Assertions.assertEquals("FilesOutput", connection.getSourceBinding());
        Assertions.assertEquals("http://localhost/pipeline/9d21ebd5",
                connection.getSourceComponent());
        Assertions.assertEquals("InputFiles", connection.getTargetBinding());
        Assertions.assertEquals("http://localhost/pipeline/b67542e2",
                connection.getTargetComponent());
    }

    private DesignerPipeline loadPipeline(String resourceName)
            throws StorageException {
        return ModelLoader.loadDesignerPipeline(
                TestUtils.statements(resourceName));
    }

}

