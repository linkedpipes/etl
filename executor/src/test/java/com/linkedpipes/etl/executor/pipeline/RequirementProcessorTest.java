package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.model.ClosableRdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URI;

public class RequirementProcessorTest {

    @Test
    public void workingAndInputDirectory() throws Exception {
        ClosableRdfSource source = Rdf4jSource.createInMemory();
        RdfBuilder builder = RdfBuilder.create(
                source, "http://graph");
        ResourceManager manager = Mockito.mock(ResourceManager.class);
        File workingFile = File.createTempFile("lp-test", "");
        File inputFile = File.createTempFile("lp-test", "");
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
        String working = RdfUtils.sparqlSelectSingle(source,
                "SELECT ?v WHERE { GRAPH <http://graph> { "
                        + " ?s <" + LP_EXEC.HAS_WORKING_DIRECTORY + "> ?v "
                        + " }}", "v");
        Assertions.assertEquals(workingFile, new File(URI.create(working)));
        //
        String input = RdfUtils.sparqlSelectSingle(source,
                "SELECT ?v WHERE { GRAPH <http://graph> { "
                        + " ?s <" + LP_EXEC.HAS_INPUT_DIRECTORY + "> ?v "
                        + " }}", "v");
        Assertions.assertEquals(inputFile, new File(URI.create(input)));
        //
        source.close();
    }

}
