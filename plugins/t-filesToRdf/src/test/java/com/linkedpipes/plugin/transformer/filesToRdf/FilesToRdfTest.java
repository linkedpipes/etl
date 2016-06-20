package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.component.test.TestEnvironment;
import com.linkedpipes.etl.component.test.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.IRI;

/**
 *
 * @author Petr Škoda
 */
public class FilesToRdfTest {

    @Test
    public void transformJsonLd() throws Exception {
        final FilesToRdf dpu = new FilesToRdf();
        dpu.configuration = new FilesToRdfConfiguration();
        dpu.configuration.setCommitSize(100);

        try (final TestEnvironment env = TestEnvironment.create(dpu, TestUtils.getTempDirectory())) {
            env.bindSystemDataUnit("InputFiles", TestUtils.fileFromResource("jsonld"));
            final WritableGraphListDataUnit output = env.bindGraphListDataUnit("OutputRdf");
            //
            env.execute();
            //
            Assert.assertSame(1, output.getGraphs().size());
            final IRI graph = output.getGraphs().iterator().next();
            output.execute((connection) -> {
               Assert.assertSame(3l, connection.size(graph));
            });
        }
    }

    @Test
    public void transformTurtle() throws Exception {
        final FilesToRdf dpu = new FilesToRdf();
        dpu.configuration = new FilesToRdfConfiguration();
        dpu.configuration.setCommitSize(100);

        try (final TestEnvironment env = TestEnvironment.create(dpu, TestUtils.getTempDirectory())) {
            env.bindSystemDataUnit("InputFiles", TestUtils.fileFromResource("turtle"));
            final WritableGraphListDataUnit output = env.bindGraphListDataUnit("OutputRdf");
            //
            env.execute();
            //
            Assert.assertSame(1, output.getGraphs().size());
            final IRI graph = output.getGraphs().iterator().next();
            output.execute((connection) -> {
               Assert.assertSame(3l, connection.size(graph));
            });
        }
    }

}
