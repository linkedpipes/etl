package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.ClosableRdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.ClosableRdf4jSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.rdf.utils.rdf4j.StatementsCollector;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.unpacker.model.GraphCollection;
import com.linkedpipes.etl.storage.unpacker.model.ModelLoader;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerPipeline;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorPipeline;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Models;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class DesignerToExecutorTest {

    static class MockedTemplateSource implements TemplateSource {

        @Override
        public Collection<Statement> getDefinition(String iri) {
            String resource = "unpacker/template/definition/" +
                    getFileName(iri) + ".trig";
            try {
                return Rdf4jUtils.loadAsStatements(resource);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public Collection<Statement> getConfiguration(String iri) {
            String resource = "unpacker/template/config/" +
                    getFileName(iri) + ".trig";
            try {
                return Rdf4jUtils.loadAsStatements(resource);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public Collection<Statement> getConfigurationDescription(String iri) {
            String resource = "unpacker/template/config-description/" +
                    getFileName(iri) + ".trig";
            try {
                return Rdf4jUtils.loadAsStatements(resource);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        private String getFileName(String iri) {
            return iri.substring(iri.lastIndexOf("/components/") + 12,
                    iri.lastIndexOf("/"));
        }

    }

    static class MockedExecutionSource implements ExecutionSource {

        @Override
        public Collection<Statement> getExecution(String iri)
                throws BaseException {
            String resource = "unpacker/executions/" +
                    getFileName(iri) + ".trig";
            try {
                return Rdf4jUtils.loadAsStatements(resource);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        private String getFileName(String iri) {
            return iri.substring(iri.lastIndexOf("/executions/") + 12);
        }
    }

    private TemplateSource templateSource = new MockedTemplateSource();

    private ExecutionSource executionSource = new MockedExecutionSource();

    @Test
    public void emptyPipeline() throws Exception {
        testUnpacking("unpacker/designer/00.trig",
                "unpacker/options/run.trig",
                "unpacker/executor/00-run.trig");
    }

    @Test
    public void runSingleComponent() throws Exception {
        testUnpacking("unpacker/designer/01.trig",
                "unpacker/options/run.trig",
                "unpacker/executor/01-run.trig");
    }

    @Test
    public void runTwoConnectedComponents() throws Exception {
        testUnpacking("unpacker/designer/02.trig",
                "unpacker/options/run.trig",
                "unpacker/executor/02-run.trig");
    }

    @Test
    public void debugTwoConnectedComponents() throws Exception {
        testUnpacking("unpacker/designer/02.trig",
                "unpacker/options/debug.trig",
                "unpacker/executor/02-debug.trig");
    }

    @Test
    public void twoBranchesDebugTo() throws Exception {
        testUnpacking("unpacker/designer/03.trig",
                "unpacker/options/debug-to-a67542e2.trig",
                "unpacker/executor/03-debug-to-a67542e2.trig");
    }

    @Test
    public void twoBranchesDebugFrom() throws Exception {
        testUnpacking("unpacker/designer/03.trig",
                "unpacker/options/debug-from-69df93d8.trig",
                "unpacker/executor/03-debug-from-69df93d8.trig");
    }

    @Test
    public void twoBranchesDebugFromTo() throws Exception {
        testUnpacking("unpacker/designer/03.trig",
                "unpacker/options/debug-from-69df93d8-to-c67542e2.trig",
                "unpacker/executor/03-debug-from-69df93d8-to-c67542e2.trig");
    }

    @Test
    public void disabledComponent() throws Exception {
        testUnpacking("unpacker/designer/04.trig",
                "unpacker/options/run.trig",
                "unpacker/executor/04-run.trig");
    }

    @Test
    public void disabledComponentDebugTo() throws Exception {
        testUnpacking("unpacker/designer/04.trig",
                "unpacker/options/debug-to-c67542e2.trig",
                "unpacker/executor/04-debug-to-c67542e2.trig");
    }

    @Test
    public void dataUnitGroupTest() throws Exception {
        testUnpacking("unpacker/designer/05.trig",
                "unpacker/options/run.trig",
                "unpacker/executor/05-run.trig");
    }

    @Test
    public void runComponentAndTemplateWithoutConfiguration() throws Exception {
        testUnpacking("unpacker/designer/06.trig",
                "unpacker/options/run.trig",
                "unpacker/executor/06-run.trig");
    }

    @Test
    public void runAterConnection() throws Exception {
        testUnpacking("unpacker/designer/07.trig",
                "unpacker/options/run.trig",
                "unpacker/executor/07-run.trig");
    }

    @Test
    public void runPortGroups() throws Exception {
        testUnpacking("unpacker/designer/08.trig",
                "unpacker/options/run.trig",
                "unpacker/executor/08-run.trig");
    }

    private void testUnpacking(String pipelineResource, String optionResource,
            String unpackedResource) throws Exception {

        ClosableRdf4jSource source = Rdf4jUtils.loadAsSource(pipelineResource);
        DesignerPipeline pipeline;
        GraphCollection graphs;
        try {
            pipeline = ModelLoader.loadDesignerPipeline(source);
            graphs = ModelLoader.loadConfigurationGraphs(source, pipeline);
        } finally {
            source.close();
        }

        UnpackOptions options = loadOptions(optionResource);

        DesignerToExecutor transformer = new DesignerToExecutor(
                templateSource, executionSource);
        transformer.transform(pipeline, graphs, options);

        ExecutorPipeline actualModel = transformer.getTarget();

        StatementsCollector collector = new StatementsCollector(
                "http://localhost/pipeline");
        actualModel.write(collector);

        for (String graph : actualModel.getReferencedGraphs()) {
            graphs.get(graph).forEach((statement -> {
                collector.add(statement);
            }));
        }

        List<Statement> expected = Rdf4jUtils.loadAsStatements(
                unpackedResource);

        Rdf4jUtils.rdfEqual(expected, collector.getStatements());

        Assert.assertTrue(Models.isomorphic(expected,
                collector.getStatements()));
    }

    private UnpackOptions loadOptions(String resourceName)
            throws IOException, RdfUtilsException {
        ClosableRdfSource source = Rdf4jUtils.loadAsSource(resourceName);
        UnpackOptions unpackOptions = new UnpackOptions();
        try {
            RdfUtils.loadByType(source, "http://options", unpackOptions,
                    UnpackOptions.TYPE);
        } finally {
            source.close();
        }
        return unpackOptions;
    }

}
