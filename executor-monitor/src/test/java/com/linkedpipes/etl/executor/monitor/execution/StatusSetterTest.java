package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewFactory;
import com.linkedpipes.etl.rdf4j.Statements;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class StatusSetterTest {

    @Test
    public void updateState() {
        Execution execution = new Execution();
        execution.setIri("http://execution");
        JsonNode overview = OverviewFactory.createQueued(execution);
        execution.setOverviewJson(overview);
        Date beforeDate = new Date();
        StatusSetter.setStatus(execution, ExecutionStatus.RUNNING);
        Date afterDate = new Date();
        //
        Assert.assertEquals(ExecutionStatus.RUNNING, execution.getStatus());
        // We can not use beforeDate.before or afterDate.after,
        // the the values can be same as getLastChange.
        Assert.assertFalse(beforeDate.after(execution.getLastChange()));
        Assert.assertFalse(afterDate.before(execution.getLastChange()));
        Statements expected = Statements.ArrayList();
        expected.setDefaultGraph(execution.getListGraph());
        expected.addIri(execution.getIri(), LP_OVERVIEW.HAS_STATUS,
                ExecutionStatus.RUNNING.asStr());
        Statements actual = new Statements(execution.getOverviewStatements());
        Assert.assertTrue(actual.containsAllLogMissing(expected));
    }

}
