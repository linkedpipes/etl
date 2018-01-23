package com.linkedpipes.etl.executor.api.v1.component.task;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class GroupTaskSourceTest {

    private static class MockedTask implements GroupTask {

        private final Object group;

        public MockedTask(Object group) {
            this.group = group;
        }

        @Override
        public String getIri() {
            return null;
        }

        @Override
        public Object getGroup() {
            return group;
        }

    }

    @Test
    public void getAllFromNullGroup() {
        TaskSource<MockedTask> source = new GroupTaskSource<>(Arrays.asList(
                new MockedTask(null),
                new MockedTask(null),
                new MockedTask(null)),
                1);
        Assert.assertNotNull(source.getTask());
        Assert.assertNotNull(source.getTask());
        Assert.assertNotNull(source.getTask());
        Assert.assertNull(source.getTask());
        Assert.assertFalse(source.doesTaskFailed());
    }

    @Test
    public void testLimitForAGroup() {
        TaskSource<MockedTask> source = new GroupTaskSource<>(Arrays.asList(
                new MockedTask(1),
                new MockedTask(1),
                new MockedTask(1)),
                2);

        MockedTask first = source.getTask();
        Assert.assertNotNull(first);
        MockedTask second = source.getTask();
        Assert.assertNotNull(second);

        // We can get another as 2 are running, so return one and get one.
        Assert.assertNull(source.getTask());
        source.onTaskFinished(first);
        MockedTask third = source.getTask();
        Assert.assertNotNull(third);
        Assert.assertNull(source.getTask());

        // Finish execution.
        source.onTaskFinished(second);
        source.onTaskFinished(third);
        Assert.assertNull(source.getTask());
        Assert.assertFalse(source.doesTaskFailed());
    }

    @Test
    public void failTaskSkipOnError() {
        TaskSource<MockedTask> source = new GroupTaskSource<>(Arrays.asList(
                new MockedTask(1),
                new MockedTask(1),
                new MockedTask(2)),
                1);
        source.setSkipOnError(true);

        MockedTask first = source.getTask();
        Assert.assertNotNull(first);

        MockedTask second = source.getTask();
        Assert.assertNotNull(second);

        source.onTaskFailed(first);
        MockedTask third = source.getTask();
        Assert.assertNotNull(third);
        Assert.assertNull(source.getTask());
        Assert.assertTrue(source.doesTaskFailed());
    }

    @Test
    public void failTaskNoSkipOnError() {
        TaskSource<MockedTask> source = new GroupTaskSource<>(Arrays.asList(
                new MockedTask(1),
                new MockedTask(1),
                new MockedTask(2)),
                1);
        source.setSkipOnError(false);

        MockedTask first = source.getTask();
        Assert.assertNotNull(first);

        MockedTask second = source.getTask();
        Assert.assertNotNull(second);

        // After failure we do not get any more tasks.
        source.onTaskFailed(first);
        source.onTaskFinished(second);
        Assert.assertNull(source.getTask());
        Assert.assertTrue(source.doesTaskFailed());
    }


}
