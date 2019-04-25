package com.linkedpipes.etl.executor.monitor.debug;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.monitor.TestUtils;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;

public class DebugDataTest {

    @Test
    public void loadDebugData() {
        String executionIri = "http://execution";
        File directory = TestUtils.resource("debug");
        Execution execution = Mockito.mock(Execution.class);
        Mockito.when(execution.getIri()).thenReturn(executionIri);
        Mockito.when(execution.getDirectory()).thenReturn(directory);
        Statements statements = twoDataUnitsWithValidAndInvalidDebugFiles();
        DebugData debugData = DebugDataFactory.create(execution, statements);
        //
        Assert.assertEquals(executionIri, debugData.getExecution());
        Assert.assertEquals(directory, debugData.getExecutionDirectory());
        Assert.assertEquals(2, debugData.getDataUnits().size());
        DataUnit valid = debugData.getDataUnits().get("valid");
        valid.updateDebugDirectories(directory);
        Assert.assertNotNull(valid);
        List<File> directories = valid.getDebugDirectories();
        Assert.assertEquals(2, valid.getDebugDirectories().size());
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
        DataUnit invalid = debugData.getDataUnits().get("invalid");
        invalid.updateDebugDirectories(directory);
        Assert.assertNotNull(invalid);
        Assert.assertEquals(0, invalid.getDebugDirectories().size());
        Assert.assertEquals("123", invalid.getExecutionId());
    }

    private Statements twoDataUnitsWithValidAndInvalidDebugFiles() {
        Statements statements = Statements.arrayList();
        statements.addIri(
                "http://valid", RDF.TYPE.stringValue(), LP_EXEC.DATA_UNIT);
        statements.addString(
                "http://valid", LP_EXEC.HAS_DEBUG, "valid");
        statements.addString(
                "http://valid", LP_EXEC.HAS_DATA_PATH, "valid-info");
        statements.addIri(
                "http://invalid", RDF.TYPE.stringValue(), LP_EXEC.DATA_UNIT);
        statements.addString(
                "http://invalid", LP_EXEC.HAS_DEBUG, "invalid");
        statements.addString(
                "http://invalid", LP_EXEC.HAS_DATA_PATH, "invalid-info");
        statements.addString(
                "http://invalid",
                LP_EXEC.HAS_EXECUTION_ETL,
                "http://executions/123");
        return statements;
    }

}
