package com.linkedpipes.etl.library.pipeline;

import com.linkedpipes.etl.library.pipeline.adapter.PipelineToRdf;
import com.linkedpipes.etl.library.pipeline.adapter.RdfToRawPipeline;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.vocabulary.LP_V1;
import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class PipelineLoaderTest {

    /**
     * This is not used by all migration layers, so not all
     * templates need to be provided here.
     */
    private final Map<Resource, Resource> templateToPlugin = new HashMap<>();

    public PipelineLoaderTest() {
        String prefix = "http://etl.linkedpipes.com/resources/components/";
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        templateToPlugin.put(
                valueFactory.createIRI(prefix + "e-httpGetFile/0.0.0"),
                valueFactory.createIRI(prefix + "e-httpGetFile/0.0.0"));
        templateToPlugin.put(
                valueFactory.createIRI(prefix + "t-excelToCsv/0.0.0"),
                valueFactory.createIRI(prefix + "t-excelToCsv/0.0.0"));
        templateToPlugin.put(
                valueFactory.createIRI(
                        "http://localhost:8080/resources/components/" +
                                "1621865731462"),
                valueFactory.createIRI(
                        prefix + "t-sparqlConstructChunked/0.0.0"));
    }

    @Test
    public void loadPipelineV0() throws Exception {
        PipelineLoader loader = loadPipeline("pipeline/v0-pipeline.trig");
        Assertions.assertFalse(loader.hasAnyFailed());
        Assertions.assertEquals(1, loader.getContainers().size());
        Assertions.assertEquals(1, loader.getMigratedPipelines().size());
        var container = loader.getContainers().get(0);

        Statements expected = Statements.arrayList();
        expected.file().addAll(TestUtils.file("pipeline/v0-pipeline-v5.trig"));

        // Add dynamic data.
        var builder = expected.builder();
        builder.setDefaultGraph(container.pipeline().resource());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_CREATED,
                container.pipeline().created());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_LAST_UPDATE,
                container.pipeline().lastUpdate());

        TestUtils.assertIsomorphic(
                expected.asList(),
                PipelineToRdf.asRdf(container.pipeline()).asList());
    }

    protected PipelineLoader loadPipeline(String fileName) throws Exception {
        PipelineLoader result = new PipelineLoader(templateToPlugin);
        Statements statements = Statements.arrayList();
        statements.file().addAll(TestUtils.file(fileName));
        result.loadAndMigrate(statements);
        return result;
    }

    @Test
    public void loadPipelineV1() throws Exception {
        PipelineLoader loader = loadPipeline("pipeline/v1-pipeline.trig");
        Assertions.assertFalse(loader.hasAnyFailed());
        Assertions.assertEquals(1, loader.getContainers().size());
        Assertions.assertEquals(1, loader.getMigratedPipelines().size());
        var container = loader.getContainers().get(0);

        Statements expected = Statements.arrayList();
        expected.file().addAll(TestUtils.file("pipeline/v1-pipeline-v5.trig"));

        // Add dynamic data.
        var builder = expected.builder();
        builder.setDefaultGraph(container.pipeline().resource());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_CREATED,
                container.pipeline().created());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_LAST_UPDATE,
                container.pipeline().lastUpdate());

        TestUtils.assertIsomorphic(
                expected.asList(),
                PipelineToRdf.asRdf(container.pipeline()).asList());
    }

    @Test
    public void loadPipelineV2() throws Exception {
        PipelineLoader loader = loadPipeline("pipeline/v2-pipeline.trig");
        Assertions.assertFalse(loader.hasAnyFailed());
        Assertions.assertEquals(1, loader.getContainers().size());
        Assertions.assertEquals(1, loader.getMigratedPipelines().size());
        var container = loader.getContainers().get(0);

        Statements expected = Statements.arrayList();
        expected.file().addAll(TestUtils.file("pipeline/v2-pipeline-v5.trig"));

        // Add dynamic data.
        var builder = expected.builder();
        builder.setDefaultGraph(container.pipeline().resource());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_CREATED,
                container.pipeline().created());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_LAST_UPDATE,
                container.pipeline().lastUpdate());

        TestUtils.assertIsomorphic(
                expected.asList(),
                PipelineToRdf.asRdf(container.pipeline()).asList());
    }

    @Test
    public void loadPipelineV3() throws Exception {
        PipelineLoader loader = loadPipeline("pipeline/v3-pipeline.trig");
        Assertions.assertFalse(loader.hasAnyFailed());
        Assertions.assertEquals(1, loader.getContainers().size());
        Assertions.assertEquals(1, loader.getMigratedPipelines().size());
        var container = loader.getContainers().get(0);

        Statements expected = Statements.arrayList();
        expected.file().addAll(TestUtils.file("pipeline/v3-pipeline-v5.trig"));

        // Add dynamic data.
        var builder = expected.builder();
        builder.setDefaultGraph(container.pipeline().resource());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_CREATED,
                container.pipeline().created());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_LAST_UPDATE,
                container.pipeline().lastUpdate());

        TestUtils.assertIsomorphic(
                expected.asList(),
                PipelineToRdf.asRdf(container.pipeline()).asList());
    }

    @Test
    public void loadPipelineV4() throws Exception {
        PipelineLoader loader = loadPipeline("pipeline/v4-pipeline.trig");
        Assertions.assertFalse(loader.hasAnyFailed());
        Assertions.assertEquals(1, loader.getContainers().size());
        Assertions.assertEquals(1, loader.getMigratedPipelines().size());
        var container = loader.getContainers().get(0);

        Statements expected = Statements.arrayList();
        expected.file().addAll(TestUtils.file("pipeline/v4-pipeline-v5.trig"));

        // Add dynamic data.
        var builder = expected.builder();
        builder.setDefaultGraph(container.pipeline().resource());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_CREATED,
                container.pipeline().created());
        builder.add(container.pipeline().resource(),
                LP_V1.HAS_LAST_UPDATE,
                container.pipeline().lastUpdate());

        TestUtils.assertIsomorphic(
                expected.asList(),
                PipelineToRdf.asRdf(container.pipeline()).asList());
    }

}
