package com.linkedpipes.etl.executor.monitor.debug.http;

import com.linkedpipes.etl.executor.monitor.TestUtils;
import com.linkedpipes.etl.executor.monitor.debug.DataUnit;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.executor.monitor.debug.DebugDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpDebugFilesTest {

    private static final String EXECUTION = "0000-1110";

    private static final String DATA_UNIT = "DU";

    private HttpDebugFilesFacade debugFacade;

    private DebugDataSource dataSource;

    @Before
    public void prepare() {
        File root = TestUtils.resource("debug");

        DataUnit dataUnit = new DataUnit(DATA_UNIT, "content", null, null);
        dataUnit.updateDebugDirectories(root);

        Map<String, DataUnit> dataUnits = new HashMap<>();
        dataUnits.put(DATA_UNIT, dataUnit);

        DebugData debugData = new DebugData(EXECUTION, root, dataUnits);

        dataSource = Mockito.mock(DebugDataSource.class);
        Mockito.when(dataSource.getDebugData(EXECUTION)).thenReturn(debugData);

        debugFacade = new HttpDebugFilesFacade(dataSource);
    }

    @Test
    public void resolveMissingExecution() {
        Assert.assertFalse(debugFacade.resolve("").isPresent());
        Assert.assertFalse(debugFacade.resolve("0000").isPresent());
    }

    @Test
    public void pathWithDots() {
        String path = EXECUTION + "/directory/../directory";
        Assert.assertFalse(debugFacade.resolve(path).isPresent());
    }

    @Test
    public void resolveExecutionRoot() {
        String path = EXECUTION;
        DebugEntry entry = debugFacade.resolve(path).orElse(null);
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry instanceof ExecutionRootEntry);
        ExecutionRootEntry content = (ExecutionRootEntry) entry;
        Assert.assertEquals(
                dataSource.getDebugData(EXECUTION),
                content.debugData);
    }

    @Test
    public void resolveDataUnit() {
        String path = EXECUTION + "/" + DATA_UNIT;
        DebugEntry entry = debugFacade.resolve(path).orElse(null);
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry instanceof DataUnitRootEntry);
        DataUnitRootEntry content = (DataUnitRootEntry) entry;
        Assert.assertEquals(
                dataSource.getDebugData(EXECUTION)
                        .getDataUnits().get(DATA_UNIT),
                content.dataUnit
        );
    }

    @Test
    public void resolveFile() throws IOException {
        String path = EXECUTION + "/" + DATA_UNIT + "/file.txt";
        DebugEntry entry = debugFacade.resolve(path).orElse(null);
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry instanceof FileContentEntry);
        FileContentEntry content = (FileContentEntry) entry;
        File expectedPath = new File(
                TestUtils.resource("debug"),
                "content/001/file.txt");
        Assert.assertTrue(Files.isSameFile(
                expectedPath.toPath(),
                content.file.toPath()));
    }

    @Test
    public void resolveDirectory() throws IOException {
        String path = EXECUTION + "/" + DATA_UNIT + "/other";
        DebugEntry entry = debugFacade.resolve(path).orElse(null);
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry instanceof DirectoryEntry);
        DirectoryEntry content = (DirectoryEntry) entry;
        File expectedPath = new File(
                TestUtils.resource("debug"),
                "content/001/other");
        Assert.assertTrue(Files.isSameFile(
                expectedPath.toPath(),
                content.directory.toPath()));
        Assert.assertEquals("001", content.source);
        //
        ResponseContent response = DirectoryEntry.prepareResponse(
                content.directory, content.source, null, null, 0, 99);
        Assert.assertEquals(1, response.metadata.count);
        Assert.assertEquals(ResponseContent.TYPE_DIR, response.metadata.type);
        Assert.assertEquals("001", response.data.get(0).source);
        Assert.assertEquals("file.txt", response.data.get(0).name);
        Assert.assertEquals(
                ResponseContent.TYPE_FILE, response.data.get(0).type);

        return;
    }

    @Test
    public void resolveAmbiguousFile() throws IOException {
        String path = EXECUTION + "/" + DATA_UNIT + "/directory/ambiguous.txt";
        DebugEntry entry = debugFacade.resolve(path).orElse(null);
        Assert.assertTrue(entry instanceof AmbiguousEntry);
        AmbiguousEntry content = (AmbiguousEntry) entry;
        Assert.assertEquals(2, content.entries.size());
        //
        DebugEntry entry000 = entry.prepareData(null, "000", 0, 999);
        Assert.assertTrue(entry000 instanceof FileContentEntry);
        FileContentEntry file000 = (FileContentEntry) entry000;
        Assert.assertEquals("000", file000.source);
        Assert.assertTrue(Files.isSameFile(
                (new File(
                        TestUtils.resource("debug"),
                        "content/000/directory/ambiguous.txt")).toPath(),
                file000.file.toPath()));

        DebugEntry entry001 = entry.prepareData(null, "001", 0, 999);
        Assert.assertTrue(entry001 instanceof FileContentEntry);
        FileContentEntry file001 = (FileContentEntry) entry001;
        Assert.assertEquals("001", file001.source);
        Assert.assertTrue(Files.isSameFile(
                (new File(
                        TestUtils.resource("debug"),
                        "content/001/directory/ambiguous.txt")).toPath(),
                file001.file.toPath()));
    }

    @Test
    public void filteringAmbiguousFile() {
        String path = EXECUTION + "/" + DATA_UNIT + "/directory/ambiguous.txt";
        DebugEntry entry = debugFacade.resolve(path).orElse(null);
        Assert.assertTrue(entry instanceof AmbiguousEntry);
        AmbiguousEntry content = (AmbiguousEntry) entry;
        Assert.assertEquals(2, content.entries.size());
        ResponseContent response;
        //
        response = content.prepareResponseContent(null, null, 0, 20);
        Assert.assertEquals(2, response.data.size());

        response = content.prepareResponseContent(null, null, 0, 1);
        Assert.assertEquals(1, response.data.size());
        Assert.assertEquals("000", response.data.get(0).source);

        response = content.prepareResponseContent(null, "000", 0, 10);
        Assert.assertEquals(1, response.data.size());
        Assert.assertEquals("000", response.data.get(0).source);

        response = content.prepareResponseContent(null, null, 0, 1);
        Assert.assertEquals(1, response.data.size());
        Assert.assertEquals("000", response.data.get(0).source);

        response = content.prepareResponseContent(null, null, 1, 1);
        Assert.assertEquals(1, response.data.size());
        Assert.assertEquals("001", response.data.get(0).source);

        response = content.prepareResponseContent(null, "001", 0, 10);
        Assert.assertEquals(1, response.data.size());
        Assert.assertEquals("001", response.data.get(0).source);

        response = content.prepareResponseContent(null, null, 3, 1);
        Assert.assertEquals(0, response.data.size());
    }

    @Test
    public void resolveAmbiguousDirectory() throws IOException {
        String path = EXECUTION + "/" + DATA_UNIT + "/directory";
        DebugEntry entry = debugFacade.resolve(path).orElse(null);
        Assert.assertTrue(entry instanceof AmbiguousEntry);
        AmbiguousEntry content = (AmbiguousEntry) entry;
        Assert.assertEquals(2, content.entries.size());
        //
        DebugEntry entry000 = entry.prepareData(null, "000", 0, 999);
        Assert.assertTrue(entry000 instanceof DirectoryEntry);
        DirectoryEntry dir000 = (DirectoryEntry) entry000;
        Assert.assertEquals("000", dir000.source);
        Assert.assertTrue(Files.isSameFile(
                (new File(
                        TestUtils.resource("debug"),
                        "content/000/directory")).toPath(),
                dir000.directory.toPath()));

        DebugEntry entry001 = entry.prepareData(null, "001", 0, 999);
        Assert.assertTrue(entry001 instanceof DirectoryEntry);
        DirectoryEntry dir001 = (DirectoryEntry) entry001;
        Assert.assertEquals("001", dir001.source);
        Assert.assertTrue(Files.isSameFile(
                (new File(
                        TestUtils.resource("debug"),
                        "content/001/directory")).toPath(),
                dir001.directory.toPath()));
    }

}
