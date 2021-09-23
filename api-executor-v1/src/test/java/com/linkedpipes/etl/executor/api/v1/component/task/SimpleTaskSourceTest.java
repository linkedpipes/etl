package com.linkedpipes.etl.executor.api.v1.component.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertNotNull(source.getTask());
        Assertions.assertNotNull(source.getTask());
        Assertions.assertNotNull(source.getTask());
        Assertions.assertNull(source.getTask());
        Assertions.assertFalse(source.doesTaskFailed());
    }

    @Test
    public void getTasks() {
        SimpleTaskSource<MockedTask> source = new SimpleTaskSource<>(
                Arrays.asList(
                        new MockedTask(), new MockedTask(), new MockedTask()));
        MockedTask firstTask = source.getTask();
        Assertions.assertNotNull(firstTask);
        source.onTaskFinished(firstTask);

        MockedTask secondTask = source.getTask();
        Assertions.assertNotNull(secondTask);
        source.onTaskFinished(secondTask);

        MockedTask thirdTask = source.getTask();
        Assertions.assertNotNull(thirdTask);
        source.onTaskFinished(thirdTask);

        Assertions.assertFalse(source.doesTaskFailed());
    }

    @Test
    public void taskFailed() {
        SimpleTaskSource<MockedTask> source = new SimpleTaskSource<>(
                Arrays.asList(new MockedTask(), new MockedTask()));
        MockedTask firstTask = source.getTask();
        Assertions.assertNotNull(firstTask);
        source.onTaskFailed(firstTask);

        MockedTask secondTask = source.getTask();
        Assertions.assertNull(secondTask);

        Assertions.assertTrue(source.doesTaskFailed());
    }

    @Test
    public void taskFailedSkipOnError() {
        SimpleTaskSource<MockedTask> source = new SimpleTaskSource<>(
                Arrays.asList(new MockedTask(), new MockedTask()));
        source.setSkipOnError(true);
        MockedTask firstTask = source.getTask();
        Assertions.assertNotNull(firstTask);
        source.onTaskFailed(firstTask);

        MockedTask secondTask = source.getTask();
        Assertions.assertNotNull(secondTask);

        Assertions.assertTrue(source.doesTaskFailed());
    }


}
