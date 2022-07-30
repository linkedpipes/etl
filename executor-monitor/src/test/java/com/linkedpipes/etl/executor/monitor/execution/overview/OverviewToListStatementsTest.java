package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_LIST;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.rdf.StatementsCompare;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class OverviewToListStatementsTest {

    private final DateFormat dateFormat = new
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private OverviewToListStatements toStatements =
            new OverviewToListStatements();

    private OverviewFactory overviewFactory = new OverviewFactory();

    @Test
    public void deleted() {
        Execution execution = Mockito.mock(Execution.class);
        JsonNode node = overviewFactory.createQueued(execution);
        Mockito.when(execution.getStatus()).thenReturn(ExecutionStatus.DELETED);
        String graph = "http://graph";
        Mockito.when(execution.getListGraph()).thenReturn(graph);
        String iri = "http://execution";
        Mockito.when(execution.getIri()).thenReturn(iri);
        Statements actual = this.toStatements.asStatements(execution, node);
        //
        StatementsBuilder expected = Statements.arrayList().builder();
        expected.setDefaultGraph(graph);
        expected.addIri(iri, RDF.TYPE, LP_LIST.TOMBSTONE);
        Assertions.assertTrue(StatementsCompare.equal(expected, actual));
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
        root.put("executionStarted", dateFormat.format(start));
        root.put("executionFinished", dateFormat.format(finished));
        root.put("directorySize", 1204);
        root.set("pipelineProgress", mapper.createObjectNode()
                .put("current", 3)
                .put("total", 10));
        Date lastChange =
                new GregorianCalendar(2016, 1, 5, 13, 6, 30).getTime();
        root.put("lastChange", dateFormat.format(lastChange));

        Execution execution = Mockito.mock(Execution.class);
        String graph = "http://graph";
        Mockito.when(execution.getListGraph()).thenReturn(graph);
        String iri = "http://execution";
        Mockito.when(execution.getIri()).thenReturn(iri);
        //
        StatementsBuilder expected = Statements.arrayList().builder();
        expected.setDefaultGraph(graph);
        expected.addIri(iri, RDF.TYPE, LP_EXEC.EXECUTION);
        expected.add(iri, LP_EXEC.HAS_SIZE, 1204L);
        expected.addIri(iri, LP_OVERVIEW.HAS_PIPELINE, pipeline);
        expected.add(iri, LP_OVERVIEW.HAS_START, start);
        expected.add(iri, LP_OVERVIEW.HAS_END, finished);
        expected.add(iri, LP_OVERVIEW.HAS_PROGRESS_CURRENT, 3);
        expected.add(iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL, 10);

        expected.addIri(
                iri, LP_OVERVIEW.HAS_STATUS, ExecutionStatus.QUEUED.asStr());
        Statements actual = this.toStatements.asStatements(execution, root);
        Assertions.assertTrue(StatementsCompare.equal(expected, actual));
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
        root.put("executionStarted", dateFormat.format(start));
        root.put("executionFinished", dateFormat.format(finished));
        root.put("directorySize", 1204);
        root.set("pipelineProgress", mapper.createObjectNode()
                .put("current", 3)
                .put("total", 10)
                .put("total_map", 5)
                .put("current_mapped", 2)
                .put("current_executed", 1));
        Date lastChange =
                new GregorianCalendar(2016, 1, 5, 13, 6, 30).getTime();
        root.put("lastChange", dateFormat.format(lastChange));

        Execution execution = Mockito.mock(Execution.class);
        String graph = "http://graph";
        Mockito.when(execution.getListGraph()).thenReturn(graph);
        String iri = "http://execution";
        Mockito.when(execution.getIri()).thenReturn(iri);
        //
        StatementsBuilder expected = Statements.arrayList().builder();
        expected.setDefaultGraph(graph);
        expected.addIri(iri, RDF.TYPE, LP_EXEC.EXECUTION);
        expected.add(iri, LP_EXEC.HAS_SIZE, 1204L);
        expected.addIri(iri, LP_OVERVIEW.HAS_PIPELINE, pipeline);
        expected.add(iri, LP_OVERVIEW.HAS_START, start);
        expected.add(iri, LP_OVERVIEW.HAS_END, finished);
        expected.add(iri, LP_OVERVIEW.HAS_PROGRESS_CURRENT, 3);
        expected.add(iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL, 10);
        expected.add(iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL_MAP, 5);
        expected.add(iri, LP_OVERVIEW.HAS_PROGRESS_MAPPED, 2);
        expected.add(iri, LP_OVERVIEW.HAS_PROGRESS_EXECUTED, 1);

        expected.addIri(
                iri, LP_OVERVIEW.HAS_STATUS, ExecutionStatus.QUEUED.asStr());
        Statements actual = this.toStatements.asStatements(execution, root);
        Assertions.assertTrue(StatementsCompare.equal(expected, actual));
    }

}
