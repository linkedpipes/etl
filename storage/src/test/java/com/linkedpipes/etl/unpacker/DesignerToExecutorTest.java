package com.linkedpipes.etl.unpacker;

//import com.linkedpipes.etl.library.rdf.Statements;
//import com.linkedpipes.etl.library.rdf.StatementsBuilder;
//import com.linkedpipes.etl.library.rdf.StatementsCompare;
//import com.linkedpipes.etl.library.rdf.StatementsSelector;
//import com.linkedpipes.etl.storage.StorageException;
//import com.linkedpipes.etl.storage.TestUtils;
//import com.linkedpipes.etl.storage.rdf.RdfUtils;
//import com.linkedpipes.etl.unpacker.DesignerToExecutor;
//import com.linkedpipes.etl.unpacker.ExecutionSource;
//import com.linkedpipes.etl.unpacker.TemplateSource;
//import com.linkedpipes.etl.unpacker.unpacker.UnpackOptions;
//import com.linkedpipes.etl.unpacker.model.GraphCollection;
//import com.linkedpipes.etl.unpacker.model.ModelLoader;
//import com.linkedpipes.etl.unpacker.model.designer.DesignerPipeline;
//import com.linkedpipes.etl.unpacker.model.executor.ExecutorPipeline;
//import com.linkedpipes.etl.unpacker.rdf.Loadable;
//import org.eclipse.rdf4j.model.Resource;
//import org.eclipse.rdf4j.model.Statement;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.util.Collection;
//
//public class DesignerToExecutorTest {
//
//    static class MockedTemplateSource implements TemplateSource {
//
//        @Override
//        public Collection<Statement> getDefinition(String iri) {
//            String resource = "unpacker/template/definition/"
//                    + getFileName(iri) + ".trig";
//            try {
//                return TestUtils.statements(resource);
//            } catch (RdfUtils.RdfException ex) {
//                throw new RuntimeException(ex);
//            }
//        }
//
//        @Override
//        public Collection<Statement> getConfiguration(String iri) {
//            String resource = "unpacker/template/config/"
//                    + getFileName(iri) + ".trig";
//            try {
//                return TestUtils.statements(resource);
//            } catch (RdfUtils.RdfException ex) {
//                throw new RuntimeException(ex);
//            }
//        }
//
//        @Override
//        public Collection<Statement> getConfigurationDescription(String iri) {
//            String resource = "unpacker/template/config-description/"
//                    + getFileName(iri) + ".trig";
//            try {
//                return TestUtils.statements(resource);
//            } catch (RdfUtils.RdfException ex) {
//                throw new RuntimeException(ex);
//            }
//        }
//
//        private String getFileName(String iri) {
//            return iri.substring(iri.lastIndexOf("/components/") + 12,
//                    iri.lastIndexOf("/"));
//        }
//
//    }
//
//    static class MockedExecutionSource implements ExecutionSource {
//
//        @Override
//        public Collection<Statement> getExecution(String iri) {
//            String resource = "unpacker/executions/"
//                    + getFileName(iri) + ".trig";
//            try {
//                return TestUtils.statements(resource);
//            } catch (RdfUtils.RdfException ex) {
//                throw new RuntimeException(ex);
//            }
//        }
//
//        private String getFileName(String iri) {
//            return iri.substring(iri.lastIndexOf("/executions/") + 12);
//        }
//    }
//
//    private TemplateSource templateSource = new MockedTemplateSource();
//
//    private ExecutionSource executionSource = new MockedExecutionSource();
//
//    @Test
//    public void emptyPipeline() throws Exception {
//        testUnpacking("unpacker/designer/00.trig",
//                "unpacker/options/run.trig",
//                "unpacker/executor/00-run.trig");
//    }
//
//    @Test
//    public void runSingleComponent() throws Exception {
//        testUnpacking("unpacker/designer/01.trig",
//                "unpacker/options/run.trig",
//                "unpacker/executor/01-run.trig");
//    }
//
//    @Test
//    public void runTwoConnectedComponents() throws Exception {
//        testUnpacking("unpacker/designer/02.trig",
//                "unpacker/options/run.trig",
//                "unpacker/executor/02-run.trig");
//    }
//
//    @Test
//    public void debugTwoConnectedComponents() throws Exception {
//        testUnpacking("unpacker/designer/02.trig",
//                "unpacker/options/debug.trig",
//                "unpacker/executor/02-debug.trig");
//    }
//
//    @Test
//    public void twoBranchesDebugTo() throws Exception {
//        testUnpacking("unpacker/designer/03.trig",
//                "unpacker/options/debug-to-a67542e2.trig",
//                "unpacker/executor/03-debug-to-a67542e2.trig");
//    }
//
//    @Test
//    public void twoBranchesDebugFrom() throws Exception {
//        testUnpacking("unpacker/designer/03.trig",
//                "unpacker/options/debug-from-69df93d8.trig",
//                "unpacker/executor/03-debug-from-69df93d8.trig");
//    }
//
//    @Test
//    public void twoBranchesDebugFromTo() throws Exception {
//        testUnpacking("unpacker/designer/03.trig",
//                "unpacker/options/debug-from-69df93d8-to-c67542e2.trig",
//                "unpacker/executor/03-debug-from-69df93d8-to-c67542e2.trig");
//    }
//
//    @Test
//    public void disabledComponent() throws Exception {
//        testUnpacking("unpacker/designer/04.trig",
//                "unpacker/options/run.trig",
//                "unpacker/executor/04-run.trig");
//    }
//
//    @Test
//    public void disabledComponentDebugTo() throws Exception {
//        testUnpacking("unpacker/designer/04.trig",
//                "unpacker/options/debug-to-c67542e2.trig",
//                "unpacker/executor/04-debug-to-c67542e2.trig");
//    }
//
//    @Test
//    public void dataUnitGroupTest() throws Exception {
//        testUnpacking("unpacker/designer/05.trig",
//                "unpacker/options/run.trig",
//                "unpacker/executor/05-run.trig");
//    }
//
//    @Test
//    public void runComponentAndTemplateWithoutConfiguration()
//            throws Exception {
//        testUnpacking("unpacker/designer/06.trig",
//                "unpacker/options/run.trig",
//                "unpacker/executor/06-run.trig");
//    }
//
//    @Test
//    public void runAterConnection() throws Exception {
//        testUnpacking("unpacker/designer/07.trig",
//                "unpacker/options/run.trig",
//                "unpacker/executor/07-run.trig");
//    }
//
//    @Test
//    public void runPortGroups() throws Exception {
//        testUnpacking("unpacker/designer/08.trig",
//                "unpacker/options/run.trig",
//                "unpacker/executor/08-run.trig");
//    }
//
//    private void testUnpacking(
//            String pipelineFile, String optionFile,
//            String unpackedFile) throws Exception {
//        StatementsSelector pipelineStatements =
//                TestUtils.statements(pipelineFile);
//        DesignerPipeline pipeline =
//                ModelLoader.loadDesignerPipeline(pipelineStatements);
//        GraphCollection graphs =
//                ModelLoader.loadConfigurationGraphs(
//                        pipelineStatements, pipeline);
//
//        UnpackOptions options = loadOptions(optionFile);
//
//        DesignerToExecutor transformer = new DesignerToExecutor(
//                templateSource, executionSource);
//        transformer.transform(pipeline, graphs, options);
//
//        ExecutorPipeline actualModel = transformer.getTarget();
//
//        StatementsBuilder actual = Statements.arrayList().builder();
//        actual.setDefaultGraph("http://localhost/pipeline");
//        actualModel.write(actual);
//
//        for (String graph : actualModel.getReferencedGraphs()) {
//            actual.addAll(graphs.get(graph));
//        }
//
//        Statements expected = TestUtils.statements(unpackedFile);
//        Assertions.assertTrue(StatementsCompare.equal(expected, actual));
//    }
//
//    private UnpackOptions loadOptions(String resourceName)
//            throws StorageException {
//        StatementsSelector selector = TestUtils.statements(resourceName);
//        Collection<Resource> resources =
//                selector.selectByType(UnpackOptions.TYPE)
//                        .selector().selectByGraph("http://options")
//                        .subjects();
//        if (resources.size() != 1) {
//            throw new StorageException("Invalid number of resources.");
//        }
//        UnpackOptions unpackOptions = new UnpackOptions();
//        Loadable.load(selector, unpackOptions, resources.iterator().next());
//        return unpackOptions;
//    }
//
//}
