package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewFactory;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.rdf.StatementsCompare;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class StatusSetterTest {

    @Test
    public void updateState() {
        Execution execution = new Execution();
        execution.setIri("http://execution");
        OverviewFactory overviewFactory = new OverviewFactory();
        JsonNode overview = overviewFactory.createQueued(execution);
        execution.setOverviewJson(overview);
        Date beforeDate = new Date();
        StatusSetter.setStatus(execution, ExecutionStatus.RUNNING);
        Date afterDate = new Date();
        //
        Assertions.assertEquals(ExecutionStatus.RUNNING, execution.getStatus());
        // We can not use beforeDate.before or afterDate.after,
        // the the values can be same as getLastChange.
        Assertions.assertFalse(beforeDate.after(execution.getLastChange()));
        Assertions.assertFalse(afterDate.before(execution.getLastChange()));
        StatementsBuilder expected = Statements.arrayList().builder();
        expected.setDefaultGraph(execution.getListGraph());
        expected.addIri(execution.getIri(), LP_OVERVIEW.HAS_STATUS,
                ExecutionStatus.RUNNING.asStr());
        Statements actual = Statements.wrap(execution.getOverviewStatements());
        Assertions.assertTrue(StatementsCompare.equal(expected, actual));
    }

}
