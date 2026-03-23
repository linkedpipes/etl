package com.linkedpipes.plugin.transformer.rdfdifftoevent;

import com.linkedpipes.etl.test.TestEnvironment;
import com.linkedpipes.etl.test.TestUtils;
import com.linkedpipes.etl.test.dataunit.TestSingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.Rdf4jDataUnit;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecutionTest {

    @Test
    public void executeUnionFromLeftAndRight() throws Exception {
        try (TestEnvironment environment = TestEnvironment.create(
                new RdfDiffToEvents(), TestUtils.getTempDirectory())) {
            TestSingleGraphDataUnit left =
                    environment.bindSingleGraphDataUnit("LeftRdf");
            TestSingleGraphDataUnit right =
                    environment.bindSingleGraphDataUnit("RightRdf");
            TestSingleGraphDataUnit output =
                    environment.bindSingleGraphDataUnit("OutputRdf");

            addStatement(left, "http://example.com/left");
            addStatement(right, "http://example.com/right");

            environment.execute();

            long outputSize = output.execute(
                    (Rdf4jDataUnit.RepositoryFunction<Long>) (connection) ->
                            connection.size(output.getReadGraph()));
            assertEquals(2L, outputSize);

            assertTrue(hasStatement(output, "http://example.com/left"));
            assertTrue(hasStatement(output, "http://example.com/right"));
        }
    }

    @Test
    public void executeUnionFromFixtures() throws Exception {
        try (TestEnvironment environment = TestEnvironment.create(
                new RdfDiffToEvents(), TestUtils.getTempDirectory())) {
            TestSingleGraphDataUnit left =
                    environment.bindSingleGraphDataUnit("LeftRdf");
            TestSingleGraphDataUnit right =
                    environment.bindSingleGraphDataUnit("RightRdf");
            TestSingleGraphDataUnit output =
                    environment.bindSingleGraphDataUnit("OutputRdf");

            TestUtils.load(
                    left,
                    TestUtils.fileFromResource("rdfdifftoevent/left.ttl"),
                    RDFFormat.TURTLE);
            TestUtils.load(
                    right,
                    TestUtils.fileFromResource("rdfdifftoevent/right.ttl"),
                    RDFFormat.TURTLE);

            environment.execute();

            assertTrue(hasStatement(output, "http://example.com/leftSubject"));
            assertTrue(hasStatement(output, "http://example.com/rightSubject"));
        }
    }

    private void addStatement(TestSingleGraphDataUnit dataUnit, String subject)
            throws Exception {
        var valueFactory = SimpleValueFactory.getInstance();
        dataUnit.execute((Rdf4jDataUnit.RepositoryProcedure) (connection) ->
                connection.add(
                        valueFactory.createIRI(subject),
                        valueFactory.createIRI("http://example.com/predicate"),
                        valueFactory.createIRI("http://example.com/object"),
                        dataUnit.getWriteGraph()));
    }

    private boolean hasStatement(TestSingleGraphDataUnit dataUnit, String subject)
            throws Exception {
        var valueFactory = SimpleValueFactory.getInstance();
        return dataUnit.execute(
                (Rdf4jDataUnit.RepositoryFunction<Boolean>) (connection) ->
                        connection.hasStatement(
                                valueFactory.createIRI(subject),
                                valueFactory.createIRI("http://example.com/predicate"),
                                valueFactory.createIRI("http://example.com/object"),
                                false,
                                dataUnit.getReadGraph()));
    }

}