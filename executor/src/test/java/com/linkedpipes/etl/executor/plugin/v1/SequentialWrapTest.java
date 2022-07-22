package com.linkedpipes.etl.executor.plugin.v1;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.service.WorkingDirectory;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SequentialWrapTest {

    public static class TestComponent implements SequentialExecution {

        @Component.InputPort(iri = "http://dataUnit/input")
        public DataUnit input;

        @Component.InputPort(iri = "http://dataUnit/output")
        public DataUnit output;

        @Component.Inject
        public ProgressReport progressReport;

        @Component.Inject
        public WorkingDirectory workingDirectory;

        boolean executed = false;

        @Override
        public void execute(Component.Context context) {
            executed = true;
        }

    }

    @Test
    public void injectAndExecute() throws LpException, IOException {
        RdfSource rdfSource = Mockito.mock(RdfSource.class);
        File path = new File(File.createTempFile("lp-test-", ""), "working");


        RdfValue pathValue = Mockito.mock(RdfValue.class);
        Mockito.when(pathValue.asString()).thenReturn(path.toURI().toString());
        Mockito.when(rdfSource.getPropertyValues(
                "http://component", LP.HAS_WORKING_DIRECTORY))
                .thenReturn(Arrays.asList(pathValue));

        Map<String, DataUnit> dataUnits = new HashMap<>();
        DataUnit input = Mockito.mock(DataUnit.class);
        Mockito.when(input.getBinding()).thenReturn("http://dataUnit/input");
        dataUnits.put("http://dataUnit/input", input);
        DataUnit output = Mockito.mock(DataUnit.class);
        Mockito.when(output.getBinding()).thenReturn("http://dataUnit/output");
        dataUnits.put("http://dataUnit/output", output);
        TestComponent component = new TestComponent();
        ComponentV1 wrap = new ComponentV1(
                component, "http://component", rdfSource);
        //
        wrap.initialize(dataUnits, null);
        Assertions.assertNotNull(component.input);
        Assertions.assertEquals(input, component.input);
        Assertions.assertEquals(output, component.output);
        Assertions.assertNotNull(component.progressReport);
        Assertions.assertNotNull(component.workingDirectory);
        Assertions.assertEquals(path.getAbsolutePath(),
                component.workingDirectory.getAbsolutePath());
        //
        Assertions.assertFalse(component.executed);
        wrap.execute(null);
        Assertions.assertTrue(component.executed);
        //
        path.delete();
    }

}
