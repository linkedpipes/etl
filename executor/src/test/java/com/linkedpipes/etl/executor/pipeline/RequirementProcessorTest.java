package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.rdf.utils.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URI;

public class RequirementProcessorTest {

    @Test
    public void workingAndInputDirectory() throws Exception {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = new RdfBuilder(
                source.getTripleWriter("http://graph"));
        final ResourceManager manager = Mockito.mock(ResourceManager.class);
        final File workingFile = File.createTempFile("lp-test", "");
        final File inputFile = File.createTempFile("lp-test", "");
        //
        Mockito.when(manager.getInputDirectory()).thenReturn(inputFile);
        Mockito.when(manager.getWorkingDirectory(Mockito.anyString()))
                .thenReturn(workingFile);
        //
        builder.entity("http://entity/a").iri(LP_PIPELINE.HAS_REQUIREMENT,
                LP_PIPELINE.WORKING_DIRECTORY);
        builder.entity("http://entity/c").iri(LP_PIPELINE.HAS_REQUIREMENT,
                LP_PIPELINE.INPUT_DIRECTORY);
        builder.commit();
        //
        RequirementProcessor.handle(source, "http://graph", manager);
        //
        final String working = RdfUtils.sparqlSelectSingle(source,
                "SELECT ?v WHERE { GRAPH <http://graph> { " +
                        " ?s <" + LP_EXEC.HAS_WORKING_DIRECTORY + "> ?v " +
                        " }}", "v");
        Assert.assertEquals(workingFile, new File(URI.create(working)));
        //
        final String input = RdfUtils.sparqlSelectSingle(source,
                "SELECT ?v WHERE { GRAPH <http://graph> { " +
                        " ?s <" + LP_EXEC.HAS_INPUT_DIRECTORY + "> ?v " +
                        " }}", "v");
        Assert.assertEquals(inputFile, new File(URI.create(input)));
        //
        source.shutdown();
    }

}
