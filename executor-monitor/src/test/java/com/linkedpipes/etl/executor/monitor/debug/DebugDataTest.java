package com.linkedpipes.etl.executor.monitor.debug;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.monitor.TestUtils;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;

public class DebugDataTest {

    @Test
    public void loadDebugData() {
        String executionIri = "http://execution/abc";
        File directory = TestUtils.resource("debug");
        Execution execution = Mockito.mock(Execution.class);
        Mockito.when(execution.getIri()).thenReturn(executionIri);
        Mockito.when(execution.getId()).thenReturn("abc");
        Mockito.when(execution.getDirectory()).thenReturn(directory);
        Statements statements = twoDataUnitsWithValidAndInvalidDebugFiles();
        DebugData debugData = DebugDataFactory.create(execution, statements);
        //
        Assertions.assertEquals("abc", debugData.getExecutionId());
        Assertions.assertEquals(directory, debugData.getExecutionDirectory());
        Assertions.assertEquals(2, debugData.getDataUnits().size());
        DataUnit valid = debugData.getDataUnits().get("valid");
        valid.updateDebugDirectories(directory);
        Assertions.assertNotNull(valid);
        List<File> directories = valid.getDebugDirectories();
        Assertions.assertEquals(2, valid.getDebugDirectories().size());
        Assertions.assertEquals(
                new File(directory, "valid-info"
                        + File.separator + ".."
                        + File.separator + "data"),
                directories.get(0));
        Assertions.assertEquals(
                new File(directory, "valid-info"
                        + File.separator + "."
                        + File.separator + "path"),
                directories.get(1));
        DataUnit invalid = debugData.getDataUnits().get("invalid");
        invalid.updateDebugDirectories(directory);
        Assertions.assertNotNull(invalid);
        Assertions.assertEquals(0, invalid.getDebugDirectories().size());
        Assertions.assertEquals("123", invalid.getExecutionId());
    }

    private Statements twoDataUnitsWithValidAndInvalidDebugFiles() {
        StatementsBuilder statements = Statements.arrayList().builder();
        statements.addIri(
                "http://valid", RDF.TYPE.stringValue(), LP_EXEC.DATA_UNIT);
        statements.add(
                "http://valid", LP_EXEC.HAS_DEBUG, "valid");
        statements.add(
                "http://valid", LP_EXEC.HAS_DATA_PATH, "valid-info");
        statements.addIri(
                "http://invalid", RDF.TYPE.stringValue(), LP_EXEC.DATA_UNIT);
        statements.add(
                "http://invalid", LP_EXEC.HAS_DEBUG, "invalid");
        statements.add(
                "http://invalid", LP_EXEC.HAS_DATA_PATH, "invalid-info");
        statements.add(
                "http://invalid",
                LP_EXEC.HAS_EXECUTION_ETL,
                "http://executions/123");
        return statements;
    }

}
