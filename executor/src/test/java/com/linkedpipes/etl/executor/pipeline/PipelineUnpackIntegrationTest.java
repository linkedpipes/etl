package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.pipeline.model.ExecutionType;
import com.linkedpipes.etl.executor.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import com.linkedpipes.etl.unpacker.UnpackerException;
import com.linkedpipes.etl.unpacker.executions.HttpExecutionSource;
import java.io.File;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Exercises the full raw-pipeline-to-execution-ready path: a self-contained
 * pipeline (a Designer-style pipeline with its referenced template's RDF
 * embedded, as {@code TemplateBundler} would produce) is unpacked entirely
 * within executor - no storage involved - and loaded into a {@link PipelineModel}.
 */
public class PipelineUnpackIntegrationTest {

    private static final String PIPELINE_IRI = "http://localhost/pipeline";

    private static final String COMPONENT_IRI = "http://localhost/pipeline/component";

    private static final String TEMPLATE_IRI = "http://localhost/template";

    private static final String JAR_IRI = "http://localhost/jar";

    private File directory;

    @AfterEach
    public void cleanUp() throws Exception {
        if (directory != null) {
            FileUtils.deleteDirectory(directory);
        }
    }

    /**
     * Build a self-contained bundle: a raw designer pipeline with a single
     * component, plus its plugin template's definition, default
     * configuration, and configuration description - exactly what
     * {@code TemplateBundler} embeds and {@code GraphTemplateSource} expects
     * to find, all in one file with no network access required.
     */
    private Statements bundle() {
        StatementsBuilder builder = Statements.arrayList().builder();

        builder.setDefaultGraph(PIPELINE_IRI);
        builder.addType(PIPELINE_IRI, LP_PIPELINE.PIPELINE);
        builder.add(PIPELINE_IRI, "http://www.w3.org/2004/02/skos/core#prefLabel", "Integration test pipeline");
        builder.addIri(PIPELINE_IRI, LP_PIPELINE.HAS_PROFILE, PIPELINE_IRI + "/profile");
        builder.addIri(PIPELINE_IRI, LP_PIPELINE.HAS_COMPONENT, COMPONENT_IRI);
        builder.addIri(PIPELINE_IRI + "/profile", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", LP_PIPELINE.PROFILE);
        builder.addIri(
                PIPELINE_IRI + "/profile",
                LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY,
                "http://linkedpipes.com/ontology/repository/SingleRepository");

        builder.addType(COMPONENT_IRI, LP_PIPELINE.COMPONENT);
        builder.addIri(COMPONENT_IRI, LP_PIPELINE.HAS_TEMPLATE, TEMPLATE_IRI);

        String configGraph = TEMPLATE_IRI + "/configuration";
        String configDescriptionGraph = TEMPLATE_IRI + "/configuration/desc";

        builder.setDefaultGraph(TEMPLATE_IRI);
        builder.addType(TEMPLATE_IRI, LP_V1.JAR_TEMPLATE);
        builder.addIri(TEMPLATE_IRI, LP_V1.HAS_JAR_URL, JAR_IRI);
        builder.addIri(TEMPLATE_IRI, LP_V1.HAS_CONFIGURATION_ENTITY_DESCRIPTION, configDescriptionGraph);

        builder.setDefaultGraph(configGraph);
        builder.addType(TEMPLATE_IRI + "/configuration/instance", "http://localhost/Configuration");

        builder.setDefaultGraph(configDescriptionGraph);
        builder.addType(configDescriptionGraph, "http://plugins.linkedpipes.com/ontology/ConfigurationDescription");
        builder.addIri(
                configDescriptionGraph,
                "http://plugins.linkedpipes.com/ontology/configuration/type",
                "http://localhost/Configuration");

        return builder;
    }

    @Test
    public void unpacksAndLoadsPipelineModel() throws Exception {
        directory = Files.createTempDirectory("lp-test-unpack-integration-").toFile();
        File bundleFile = new File(directory, "definition.trig");
        writeBundle(bundleFile);

        File repositoryDirectory = new File(directory, "repository");

        Pipeline pipeline = new Pipeline();
        HttpExecutionSource executionSource = new HttpExecutionSource("http://localhost:0");
        pipeline.load(bundleFile, null, repositoryDirectory, executionSource);

        PipelineModel model = pipeline.getModel();
        Assertions.assertEquals(1, model.getComponents().size());

        PipelineComponent component = model.getComponents().get(0);
        Assertions.assertEquals(COMPONENT_IRI, component.getIri());
        Assertions.assertEquals(JAR_IRI, component.getJarPath());
        Assertions.assertEquals(ExecutionType.EXECUTE, component.getExecutionType());
        Assertions.assertNotNull(component.getConfigurationGraph());
        Assertions.assertNotNull(component.getConfigurationDescription());

        pipeline.closeRepository();
    }

    private void writeBundle(File file) throws UnpackerException {
        try {
            bundle().file().writeToFile(file, RDFFormat.TRIG);
        } catch (Exception ex) {
            throw new UnpackerException("Can't write test bundle.", ex);
        }
    }
}
