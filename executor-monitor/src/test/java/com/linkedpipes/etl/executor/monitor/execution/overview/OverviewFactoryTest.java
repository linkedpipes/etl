package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

public class OverviewFactoryTest {

    @Test
    public void loadFromJsonDeleted() {
        Date date = new Date();
        Execution execution = Mockito.mock(Execution.class);
        String iri = "http://execution";
        Mockito.when(execution.getIri()).thenReturn(iri);
        OverviewObject overview = OverviewObject.fromJson(
                OverviewFactory.createDeleted(execution, date));
        //
        Assert.assertEquals(
                ExecutionStatus.DELETED.asStr(),
                overview.getStatus());
        Assert.assertEquals(date, overview.getLastChange());
    }

    @Test
    public void loadFromJsonQueued() {
        Execution execution = Mockito.mock(Execution.class);
        String iri = "http://execution";
        Mockito.when(execution.getIri()).thenReturn(iri);
        Resource pipeline = SimpleValueFactory.getInstance().createIRI(
                "http://pipeline");
        Mockito.when(execution.getPipeline()).thenReturn(pipeline);
        OverviewObject overview = OverviewObject.fromJson(
                OverviewFactory.createQueued(execution));
        //
        Assert.assertEquals(
                ExecutionStatus.QUEUED.asStr(),
                overview.getStatus());
        Assert.assertEquals(pipeline.stringValue(), overview.getPipeline());
    }

    @Test
    public void loadFromJsonQueuedNoPipeline() {
        Execution execution = Mockito.mock(Execution.class);
        String iri = "http://execution";
        Date date = new Date();
        Mockito.when(execution.getIri()).thenReturn(iri);
        Mockito.when(execution.getLastOverviewChange()).thenReturn(date);
        OverviewObject overview = OverviewObject.fromJson(
                OverviewFactory.createQueued(execution));
        //
        Assert.assertEquals(
                ExecutionStatus.QUEUED.asStr(),
                overview.getStatus());
        Assert.assertNull(overview.getPipeline());
        Assert.assertEquals(date, overview.getLastChange());
    }

}
