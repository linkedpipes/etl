package com.linkedpipes.etl.executor.api.v1.component.task;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class SimpleTaskSourceTest {

    private static class MockedTask implements Task {

        @Override
        public String getIri() {
            return null;
        }
    }

    @Test
    public void getAllTasks() {
        SimpleTaskSource<MockedTask> source = new SimpleTaskSource<>(
                Arrays.asList(
                        new MockedTask(), new MockedTask(), new MockedTask()));
        Assert.assertNotNull(source.getTask());
        Assert.assertNotNull(source.getTask());
        Assert.assertNotNull(source.getTask());
        Assert.assertNull(source.getTask());
        Assert.assertFalse(source.doesTaskFailed());
    }

    @Test
    public void getTasks() {
        SimpleTaskSource<MockedTask> source = new SimpleTaskSource<>(
                Arrays.asList(
                        new MockedTask(), new MockedTask(), new MockedTask()));
        MockedTask firstTask = source.getTask();
        Assert.assertNotNull(firstTask);
        source.onTaskFinished(firstTask);

        MockedTask secondTask = source.getTask();
        Assert.assertNotNull(secondTask);
        source.onTaskFinished(secondTask);

        MockedTask thirdTask = source.getTask();
        Assert.assertNotNull(thirdTask);
        source.onTaskFinished(thirdTask);

        Assert.assertFalse(source.doesTaskFailed());
    }

    @Test
    public void taskFailed() {
        SimpleTaskSource<MockedTask> source = new SimpleTaskSource<>(
                Arrays.asList(new MockedTask(), new MockedTask()));
        MockedTask firstTask = source.getTask();
        Assert.assertNotNull(firstTask);
        source.onTaskFailed(firstTask);

        MockedTask secondTask = source.getTask();
        Assert.assertNull(secondTask);

        Assert.assertTrue(source.doesTaskFailed());
    }

    @Test
    public void taskFailedSkipOnError() {
        SimpleTaskSource<MockedTask> source = new SimpleTaskSource<>(
                Arrays.asList(new MockedTask(), new MockedTask()));
        source.setSkipOnError(true);
        MockedTask firstTask = source.getTask();
        Assert.assertNotNull(firstTask);
        source.onTaskFailed(firstTask);

        MockedTask secondTask = source.getTask();
        Assert.assertNotNull(secondTask);

        Assert.assertTrue(source.doesTaskFailed());
    }


}
