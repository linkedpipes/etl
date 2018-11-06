package com.linkedpipes.etl.executor.monitor.debug;

import com.linkedpipes.etl.executor.monitor.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class DataUnitTest {

    @Test
    public void getExecutionId() {
        DataUnit dataUnit = new DataUnit();
        dataUnit.setMappedFromExecution("http://localhost/executions/123");
        //
        Assert.assertEquals("123", dataUnit.getExecutionId());
    }

    @Test
    public void loadDebugDirectories() {
        File directory = TestUtils.resource("debug");
        DataUnit dataUnit = new DataUnit();
        dataUnit.setRelativeDataPath("valid-info");
        dataUnit.updateDebugDirectories(directory);
        //
        Assert.assertEquals(2, dataUnit.getDebugDirectories().size());
        List<File> directories = dataUnit.getDebugDirectories();
        Assert.assertEquals(
                new File(directory, "valid-info"
                        + File.separator + ".."
                        + File.separator + "data"),
                directories.get(0));
        Assert.assertEquals(
                new File(directory, "valid-info"
                        + File.separator + "."
                        + File.separator + "path"),
                directories.get(1));

    }

    @Test
    public void loadMissingDebugDirectories() {
        File directory = TestUtils.resource("debug");
        DataUnit dataUnit = new DataUnit();
        dataUnit.setRelativeDataPath("invalid-info");
        dataUnit.updateDebugDirectories(directory);
        //
        Assert.assertEquals(0, dataUnit.getDebugDirectories().size());
    }

}
