package com.linkedpipes.plugin.loader.dcatApToCkan;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.test.TestEnvironment;
import com.linkedpipes.etl.component.test.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Klímek Jakub
 */
public class DcatApToCkanTest {

    private static final Logger LOG = LoggerFactory.getLogger(DcatApToCkanTest.class);

    @Test
    public void transformJsonLd() throws Exception {
        final DcatApToCkan dpu = new DcatApToCkan();
        dpu.configuration = new DcatApToCkanConfiguration();

        try (final TestEnvironment env = TestEnvironment.create(dpu, TestUtils.getTempDirectory())) {
            TestUtils.load(env.bindSingleGraphDataUnit("Metadata"),
                    TestUtils.fileFromResource("input.ttl"), RDFFormat.TURTLE);
            final WritableFilesDataUnit output = env.bindSystemDataUnit("OutputFiles", TestUtils.getTempDirectory());
            //
            env.execute();
        } catch (Exception ex) {
            LOG.error("Failure", ex);
        }
    }

}
