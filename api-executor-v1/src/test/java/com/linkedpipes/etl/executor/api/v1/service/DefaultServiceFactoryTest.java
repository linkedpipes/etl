package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
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
import java.nio.file.Files;

public class DefaultServiceFactoryTest {

    @Test
    public void exceptionFactory() throws LpException {
        final DefaultServiceFactory factory = new DefaultServiceFactory();
        final ExceptionFactory result = (ExceptionFactory) factory.create(
                ExceptionFactory.class, null, null, null, null);
        Assert.assertNotNull(result);
        //
        Assert.assertNotNull(result.failure("Message"));
    }

    @Test
    public void progressReport() throws LpException {
        final Component.Context context = Mockito.mock(Component.Context.class);
        final DefaultServiceFactory factory = new DefaultServiceFactory();
        final ProgressReport result = (ProgressReport) factory.create(
                ProgressReport.class, null, null, null, context);
        Assert.assertNotNull(result);
        //
        result.start(2);
        result.entryProcessed();
        result.entryProcessed();
        result.done();
        //
        Mockito.verify(context, Mockito.times(4)).sendMessage(Mockito.any());
    }

    @Test
    public void workingDirectory() throws LpException, IOException,
            RdfUtilsException {
        // We create temp file and then remove it to get name for
        // temp directory.
        final File path = Files.createTempDirectory("lp-test-").toFile();
        //
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        builder.entity("http://component")
                .iri(RDF.TYPE, LP_PIPELINE.COMPONENT)
                .iri(LP_EXEC.HAS_WORKING_DIRECTORY, path.toURI().toString());
        builder.commit();
        //
        final DefaultServiceFactory factory = new DefaultServiceFactory();
        final WorkingDirectory result = (WorkingDirectory) factory.create(
                WorkingDirectory.class, "http://component", "http://graph",
                source, null);
        Assert.assertNotNull(result);
        Assert.assertTrue(path.isDirectory());
        Assert.assertEquals(path.getAbsolutePath(), result.getAbsolutePath());
        //
        path.delete();
        source.shutdown();
    }

}
