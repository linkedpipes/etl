package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.service.DefaultServiceFactory;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.service.WorkingDirectory;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
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
        public void execute() throws LpException {
            executed = true;
        }

    }

    @Test
    public void injectAndExecute() throws LpException, IOException,
            RdfUtilsException {
        final TestComponent component = new TestComponent();
        final RdfSource source = Rdf4jSource.createInMemory();
        final ComponentInfo componentInfo = new ComponentInfo(
                "http://component", "http://graph");
        final SequentialWrap wrap = new SequentialWrap(
                component, componentInfo, source, new DefaultServiceFactory());
        final File path = File.createTempFile("lp-test-", "");
        //
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        builder.entity("http://component")
                .iri(RDF.TYPE, LP_PIPELINE.COMPONENT)
                .iri(LP_EXEC.HAS_WORKING_DIRECTORY, path.toURI().toString());
        builder.commit();
        //
        final Map<String, DataUnit> dataUnits = new HashMap<>();
        final DataUnit input = Mockito.mock(DataUnit.class);
        Mockito.when(input.getBinding()).thenReturn("http://dataUnit/input");
        dataUnits.put("http://dataUnit/input", input);
        final DataUnit output = Mockito.mock(DataUnit.class);
        Mockito.when(output.getBinding()).thenReturn("http://dataUnit/output");
        dataUnits.put("http://dataUnit/output", output);
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
        wrap.execute();
        Assert.assertTrue(component.executed);
        //
        path.delete();
        source.shutdown();
    }

}
