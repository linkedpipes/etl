package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_LIST;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class OverviewToListStatementsTest {

    private static final DateFormat DATE_FORMAT = new
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private OverviewToListStatements toStatements =
            new OverviewToListStatements();

    @Test
    public void deleted() {
        Execution execution = Mockito.mock(Execution.class);
        JsonNode node = OverviewFactory.createQueued(execution);
        Mockito.when(execution.getStatus()).thenReturn(ExecutionStatus.DELETED);
        String graph = "http://graph";
        Mockito.when(execution.getListGraph()).thenReturn(graph);
        String iri = "http://execution";
        Mockito.when(execution.getIri()).thenReturn(iri);
        Statements actual = this.toStatements.asStatements(execution, node);
        //
        Statements expected = Statements.ArrayList();
        expected.setDefaultGraph(graph);
        expected.addIri(iri, RDF.TYPE, LP_LIST.TOMBSTONE);
        Assert.assertTrue(actual.containsAllLogMissing(expected));
    }

    @Test
    public void progressPriorTo20181018() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.set("status", mapper.createObjectNode()
                .put("@id", ExecutionStatus.QUEUED.asStr()));
        String pipeline = "http://pipeline";
        root.set("pipeline", mapper.createObjectNode()
                .put("@id", pipeline));
        Date start = new GregorianCalendar(2019, 1, 1, 23, 2, 10).getTime();
        Date finished = new GregorianCalendar(2019, 1, 5, 13, 6, 30).getTime();
        root.put("executionStarted", DATE_FORMAT.format(start));
        root.put("executionFinished", DATE_FORMAT.format(finished));
        root.put("directorySize", 1204);
        root.set("pipelineProgress", mapper.createObjectNode()
                .put("current", 3)
                .put("total", 10));
        Date lastChange = new GregorianCalendar(2016, 1, 5, 13, 6, 30).getTime();
        root.put("lastChange", DATE_FORMAT.format(lastChange));

        Execution execution = Mockito.mock(Execution.class);
        String graph = "http://graph";
        Mockito.when(execution.getListGraph()).thenReturn(graph);
        String iri = "http://execution";
        Mockito.when(execution.getIri()).thenReturn(iri);
        Statements actual = this.toStatements.asStatements(execution, root);
        //
        Statements expected = Statements.ArrayList();
        expected.setDefaultGraph(graph);
        expected.addIri(iri, RDF.TYPE, LP_EXEC.EXECUTION);
        expected.addLong(iri, LP_EXEC.HAS_SIZE, 1204l);
        expected.addIri(iri, LP_OVERVIEW.HAS_PIPELINE, pipeline);
        expected.addDate(iri, LP_OVERVIEW.HAS_START, start);
        expected.addDate(iri, LP_OVERVIEW.HAS_END, finished);
        expected.addInt(iri, LP_OVERVIEW.HAS_PROGRESS_CURRENT, 3);
        expected.addInt(iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL, 10);

        expected.addIri(
                iri, LP_OVERVIEW.HAS_STATUS, ExecutionStatus.QUEUED.asStr());
        Assert.assertTrue(actual.containsAllLogMissing(expected));
    }

    @Test
    public void fullInformation() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.set("status", mapper.createObjectNode()
                .put("@id", ExecutionStatus.QUEUED.asStr()));
        String pipeline = "http://pipeline";
        root.set("pipeline", mapper.createObjectNode()
                .put("@id", pipeline));
        Date start = new GregorianCalendar(2019, 1, 1, 23, 2, 10).getTime();
        Date finished = new GregorianCalendar(2019, 1, 5, 13, 6, 30).getTime();
        root.put("executionStarted", DATE_FORMAT.format(start));
        root.put("executionFinished", DATE_FORMAT.format(finished));
        root.put("directorySize", 1204);
        root.set("pipelineProgress", mapper.createObjectNode()
                .put("current", 3)
                .put("total", 10)
                .put("total_map", 5)
                .put("current_mapped", 2)
                .put("current_executed", 1));
        Date lastChange = new GregorianCalendar(2016, 1, 5, 13, 6, 30).getTime();
        root.put("lastChange", DATE_FORMAT.format(lastChange));

        Execution execution = Mockito.mock(Execution.class);
        String graph = "http://graph";
        Mockito.when(execution.getListGraph()).thenReturn(graph);
        String iri = "http://execution";
        Mockito.when(execution.getIri()).thenReturn(iri);
        Statements actual = this.toStatements.asStatements(execution, root);
        //
        Statements expected = Statements.ArrayList();
        expected.setDefaultGraph(graph);
        expected.addIri(iri, RDF.TYPE, LP_EXEC.EXECUTION);
        expected.addLong(iri, LP_EXEC.HAS_SIZE, 1204l);
        expected.addIri(iri, LP_OVERVIEW.HAS_PIPELINE, pipeline);
        expected.addDate(iri, LP_OVERVIEW.HAS_START, start);
        expected.addDate(iri, LP_OVERVIEW.HAS_END, finished);
        expected.addInt(iri, LP_OVERVIEW.HAS_PROGRESS_CURRENT, 3);
        expected.addInt(iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL, 10);
        expected.addInt(iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL_MAP, 5);
        expected.addInt(iri, LP_OVERVIEW.HAS_PROGRESS_MAPPED, 2);
        expected.addInt(iri, LP_OVERVIEW.HAS_PROGRESS_EXECUTED, 1);

        expected.addIri(
                iri, LP_OVERVIEW.HAS_STATUS, ExecutionStatus.QUEUED.asStr());
        Assert.assertTrue(actual.containsAllLogMissing(expected));
    }

}
