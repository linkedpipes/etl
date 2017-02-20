package com.linkedpipes.plugin.loader.dcatApToCkan;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.test.TestEnvironment;
import com.linkedpipes.etl.test.TestUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DcatApToCkanTest {

    private static final Logger LOG = LoggerFactory.getLogger(DcatApToCkanTest.class);

//    @Test
    public void transformJsonLd() throws Exception {
        final DcatApToCkan dpu = new DcatApToCkan();
        dpu.configuration = new DcatApToCkanConfiguration();

        try (final TestEnvironment env = TestEnvironment.create(dpu, TestUtils.getTempDirectory())) {
            TestUtils.load(env.bindSingleGraphDataUnit("Metadata"),
                    TestUtils.fileFromResource("input.ttl"), RDFFormat.TURTLE);
            final WritableFilesDataUnit
                    output = env.bindSystemDataUnit("OutputFiles", TestUtils.getTempDirectory());
            //
            env.execute();
        } catch (Exception ex) {
            LOG.error("Failure", ex);
        }
    }

}
