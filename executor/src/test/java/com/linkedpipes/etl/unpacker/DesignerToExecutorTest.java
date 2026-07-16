package com.linkedpipes.etl.unpacker;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.rdf.StatementsCompare;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.unpacker.model.GraphCollection;
import com.linkedpipes.etl.unpacker.model.ModelLoader;
import com.linkedpipes.etl.unpacker.model.designer.DesignerPipeline;
import com.linkedpipes.etl.unpacker.model.executor.ExecutorPipeline;
import com.linkedpipes.etl.unpacker.rdf.Loadable;
import java.io.IOException;
import java.util.Collection;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DesignerToExecutorTest {

    static class MockedTemplateSource implements TemplateSource {

        @Override
        public Collection<Statement> getDefinition(String iri) throws UnpackerException {
            return load("unpacker/template/definition/" + getFileName(iri) + ".trig");
        }

        @Override
        public Collection<Statement> getConfiguration(String iri) throws UnpackerException {
            return load("unpacker/template/config/" + getFileName(iri) + ".trig");
        }

        @Override
        public Collection<Statement> getConfigurationDescription(String iri) throws UnpackerException {
            return load("unpacker/template/config-description/" + getFileName(iri) + ".trig");
        }

        private Collection<Statement> load(String resource) throws UnpackerException {
            try {
                return TestUtils.statements(resource);
            } catch (IOException ex) {
                throw new UnpackerException(ex);
            }
        }

        private String getFileName(String iri) {
            return iri.substring(iri.lastIndexOf("/components/") + 12, iri.lastIndexOf("/"));
        }
    }

    static class MockedExecutionSource implements ExecutionSource {

        @Override
        public Collection<Statement> getExecution(String iri) throws UnpackerException {
            try {
                return TestUtils.statements("unpacker/executions/" + getFileName(iri) + ".trig");
            } catch (IOException ex) {
                throw new UnpackerException(ex);
            }
        }

        private String getFileName(String iri) {
            return iri.substring(iri.lastIndexOf("/executions/") + 12);
        }
    }

    private final TemplateSource templateSource = new MockedTemplateSource();

    private final ExecutionSource executionSource = new MockedExecutionSource();

    @Test
    public void emptyPipeline() throws Exception {
        testUnpacking("unpacker/designer/00.trig", "unpacker/options/run.trig", "unpacker/executor/00-run.trig");
    }

    @Test
    public void runSingleComponent() throws Exception {
        testUnpacking("unpacker/designer/01.trig", "unpacker/options/run.trig", "unpacker/executor/01-run.trig");
    }

    @Test
    public void runTwoConnectedComponents() throws Exception {
        testUnpacking("unpacker/designer/02.trig", "unpacker/options/run.trig", "unpacker/executor/02-run.trig");
    }

    @Test
    public void debugTwoConnectedComponents() throws Exception {
        testUnpacking("unpacker/designer/02.trig", "unpacker/options/debug.trig", "unpacker/executor/02-debug.trig");
    }

    @Test
    public void twoBranchesDebugTo() throws Exception {
        testUnpacking(
                "unpacker/designer/03.trig",
                "unpacker/options/debug-to-a67542e2.trig",
                "unpacker/executor/03-debug-to-a67542e2.trig");
    }

    @Test
    public void twoBranchesDebugFrom() throws Exception {
        testUnpacking(
                "unpacker/designer/03.trig",
                "unpacker/options/debug-from-69df93d8.trig",
                "unpacker/executor/03-debug-from-69df93d8.trig");
    }

    @Test
    public void twoBranchesDebugFromTo() throws Exception {
        testUnpacking(
                "unpacker/designer/03.trig",
                "unpacker/options/debug-from-69df93d8-to-c67542e2.trig",
                "unpacker/executor/03-debug-from-69df93d8-to-c67542e2.trig");
    }

    @Test
    public void disabledComponent() throws Exception {
        testUnpacking("unpacker/designer/04.trig", "unpacker/options/run.trig", "unpacker/executor/04-run.trig");
    }

    @Test
    public void disabledComponentDebugTo() throws Exception {
        testUnpacking(
                "unpacker/designer/04.trig",
                "unpacker/options/debug-to-c67542e2.trig",
                "unpacker/executor/04-debug-to-c67542e2.trig");
    }

    @Test
    public void dataUnitGroupTest() throws Exception {
        testUnpacking("unpacker/designer/05.trig", "unpacker/options/run.trig", "unpacker/executor/05-run.trig");
    }

    @Test
    public void runComponentAndTemplateWithoutConfiguration() throws Exception {
        testUnpacking("unpacker/designer/06.trig", "unpacker/options/run.trig", "unpacker/executor/06-run.trig");
    }

    @Test
    public void runAterConnection() throws Exception {
        testUnpacking("unpacker/designer/07.trig", "unpacker/options/run.trig", "unpacker/executor/07-run.trig");
    }

    @Test
    public void runPortGroups() throws Exception {
        testUnpacking("unpacker/designer/08.trig", "unpacker/options/run.trig", "unpacker/executor/08-run.trig");
    }

    private void testUnpacking(String pipelineFile, String optionFile, String unpackedFile) throws Exception {
        StatementsSelector pipelineStatements = TestUtils.statements(pipelineFile).selector();
        DesignerPipeline pipeline = ModelLoader.loadDesignerPipeline(pipelineStatements);
        GraphCollection graphs = ModelLoader.loadConfigurationGraphs(pipelineStatements, pipeline);

        UnpackOptions options = loadOptions(optionFile);

        DesignerToExecutor transformer = new DesignerToExecutor(templateSource, executionSource);
        transformer.transform(pipeline, graphs, options);

        ExecutorPipeline actualModel = transformer.getTarget();

        StatementsBuilder actual = Statements.arrayList().builder();
        actual.setDefaultGraph("http://localhost/pipeline");
        actualModel.write(actual);

        for (String graph : actualModel.getReferencedGraphs()) {
            actual.addAll(graphs.get(graph));
        }

        Statements expected = TestUtils.statements(unpackedFile);
        Assertions.assertTrue(StatementsCompare.isIsomorphic(expected, actual));
    }

    private UnpackOptions loadOptions(String resourceName) throws UnpackerException, IOException {
        StatementsSelector selector = TestUtils.statements(resourceName).selector();
        Collection<Resource> resources =
                selector.selectByType(UnpackOptions.TYPE).selector().selectByGraph("http://options").subjects();
        if (resources.size() != 1) {
            throw new UnpackerException("Invalid number of resources.");
        }
        UnpackOptions unpackOptions = new UnpackOptions();
        Loadable.load(selector, unpackOptions, resources.iterator().next());
        return unpackOptions;
    }
}
