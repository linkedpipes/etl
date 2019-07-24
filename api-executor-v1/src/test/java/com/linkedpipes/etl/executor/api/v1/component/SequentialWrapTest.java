package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.service.DefaultServiceFactory;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.service.WorkingDirectory;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP;
import org.junit.Assert;
import org.junit.Test;
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
        public ExceptionFactory exceptionFactory;

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
        SequentialWrap wrap = new SequentialWrap(
                component, "http://component", rdfSource,
                new DefaultServiceFactory());
        //
        wrap.initialize(dataUnits, null);
        Assert.assertNotNull(component.input);
        Assert.assertEquals(input, component.input);
        Assert.assertEquals(output, component.output);
        Assert.assertNotNull(component.exceptionFactory);
        Assert.assertNotNull(component.progressReport);
        Assert.assertNotNull(component.workingDirectory);
        Assert.assertEquals(path.getAbsolutePath(),
                component.workingDirectory.getAbsolutePath());
        //
        Assert.assertFalse(component.executed);
        wrap.execute(null);
        Assert.assertTrue(component.executed);
        //
        path.delete();
    }

}
