package com.linkedpipes.plugin.transformer.rdfdifftoevent;

import com.linkedpipes.etl.dataunit.core.rdf.Rdf4jDataUnit;
import com.linkedpipes.etl.test.TestEnvironment;
import com.linkedpipes.etl.test.TestUtils;
import com.linkedpipes.etl.test.dataunit.TestSingleGraphDataUnit;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecutionTest {

    private static final String EX = "http://example.com/";
    private static final String EVENT_TYPE = RdfDiffToEvents.DIFF_EVENT_TYPE;
    private static final String CREATE     = RdfDiffToEvents.CREATE_TYPE;
    private static final String UPDATE     = RdfDiffToEvents.UPDATE_TYPE;
    private static final String DELETE     = RdfDiffToEvents.DELETE_TYPE;

    /**
     * Subject only in left  →  DELETE event (no CBD in output).
     * Subject only in right →  CREATE event (CBD written to output).
     */
    @Test
    public void deleteAndCreateEventsFromProgrammaticData() throws Exception {
        try (TestEnvironment env = TestEnvironment.create(
                new RdfDiffToEvents(), TestUtils.getTempDirectory())) {
            TestSingleGraphDataUnit left =
                    env.bindSingleGraphDataUnit("LeftRdf");
            TestSingleGraphDataUnit right =
                    env.bindSingleGraphDataUnit("RightRdf");
            TestSingleGraphDataUnit output =
                    env.bindSingleGraphDataUnit("OutputRdf");

            // ex:leftOnly  →  will be deleted
            addStatement(left,  EX + "leftOnly",   EX + "p", EX + "o");
            // ex:rightOnly →  will be created
            addStatement(right, EX + "rightOnly",  EX + "p", EX + "o");

            env.execute();

            // DELETE event for leftOnly
            assertTrue(hasEventType(output, EX + "leftOnly",  DELETE));
            // No CBD written for DELETE
            assertFalse(hasCBDTriple(output, EX + "leftOnly",  EX + "p", EX + "o"));

            // CREATE event for rightOnly
            assertTrue(hasEventType(output, EX + "rightOnly", CREATE));
            // CBD written for CREATE
            assertTrue(hasCBDTriple(output, EX + "rightOnly",  EX + "p", EX + "o"));
        }
    }

    /**
     * Fixture-based test covering all four diff outcomes:
     * <ul>
     *   <li>ex:leftOnly  – left only  → DELETE (no CBD in output)</li>
     *   <li>ex:bothSame  – both, identical CBD → NOOP  (nothing in output)</li>
     *   <li>ex:changed   – both, different CBD → UPDATE (right CBD in output)</li>
     *   <li>ex:rightOnly – right only → CREATE (right CBD in output)</li>
     * </ul>
     */
    @Test
    public void allDiffCasesFromFixtures() throws Exception {
        try (TestEnvironment env = TestEnvironment.create(
                new RdfDiffToEvents(), TestUtils.getTempDirectory())) {
            TestSingleGraphDataUnit left =
                    env.bindSingleGraphDataUnit("LeftRdf");
            TestSingleGraphDataUnit right =
                    env.bindSingleGraphDataUnit("RightRdf");
            TestSingleGraphDataUnit output =
                    env.bindSingleGraphDataUnit("OutputRdf");

            TestUtils.load(
                    left,
                    TestUtils.fileFromResource("rdfdifftoevent/left.ttl"),
                    RDFFormat.TURTLE);
            TestUtils.load(
                    right,
                    TestUtils.fileFromResource("rdfdifftoevent/right.ttl"),
                    RDFFormat.TURTLE);

            env.execute();

            // DELETE: marker present, no CBD
            assertTrue(hasEventType(output, EX + "leftOnly",  DELETE));
            assertFalse(hasEventType(output, EX + "leftOnly", CREATE));

            // NOOP: no event marker of any kind
            assertFalse(hasEventType(output, EX + "bothSame", CREATE));
            assertFalse(hasEventType(output, EX + "bothSame", UPDATE));
            assertFalse(hasEventType(output, EX + "bothSame", DELETE));

            // UPDATE: marker present, right-side CBD present
            assertTrue(hasEventType(output, EX + "changed", UPDATE));
            assertFalse(hasEventType(output, EX + "changed", CREATE));

            // CREATE: marker present, right-side CBD present
            assertTrue(hasEventType(output, EX + "rightOnly", CREATE));
            assertFalse(hasEventType(output, EX + "rightOnly", DELETE));
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void addStatement(
            TestSingleGraphDataUnit dataUnit,
            String subject, String predicate, String object) throws Exception {
        var vf = SimpleValueFactory.getInstance();
        dataUnit.execute((Rdf4jDataUnit.RepositoryProcedure) (conn) ->
                conn.add(
                        vf.createIRI(subject),
                        vf.createIRI(predicate),
                        vf.createIRI(object),
                        dataUnit.getWriteGraph()));
    }

    /** Returns true when the output contains {@code <subject> lpdiff:diffEventType <typeIri>}. */
    private boolean hasEventType(
            TestSingleGraphDataUnit dataUnit,
            String subject, String typeIri) throws Exception {
        var vf = SimpleValueFactory.getInstance();
        return dataUnit.execute(
                (Rdf4jDataUnit.RepositoryFunction<Boolean>) (conn) ->
                        conn.hasStatement(
                                vf.createIRI(subject),
                                vf.createIRI(EVENT_TYPE),
                                vf.createIRI(typeIri),
                                false,
                                dataUnit.getReadGraph()));
    }

    /** Returns true when the output contains a specific CBD triple for a subject. */
    private boolean hasCBDTriple(
            TestSingleGraphDataUnit dataUnit,
            String subject, String predicate, String object) throws Exception {
        var vf = SimpleValueFactory.getInstance();
        return dataUnit.execute(
                (Rdf4jDataUnit.RepositoryFunction<Boolean>) (conn) ->
                        conn.hasStatement(
                                vf.createIRI(subject),
                                vf.createIRI(predicate),
                                vf.createIRI(object),
                                false,
                                dataUnit.getReadGraph()));
    }
}
