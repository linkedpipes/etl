package com.linkedpipes.plugin.loader.dcatAp11ToCkan;

import org.openrdf.rio.RDFFormat;

import com.linkedpipes.etl.component.test.TestEnvironment;
import com.linkedpipes.etl.component.test.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Klímek Jakub
 */
public class DcatAp11ToCkanTest {

    private static final Logger LOG = LoggerFactory.getLogger(DcatAp11ToCkanTest.class);

    //@Test
    public void loadTest() throws Exception {
        final DcatAp11ToCkan component = new DcatAp11ToCkan();
        component.configuration = new DcatAp11ToCkanConfiguration();
        component.configuration.setApiUri("");
        component.configuration.setApiKey("");
        component.configuration.setLoadLanguage("cs");
        component.configuration.setDatasetID("test9999");
        component.configuration.setProfile("http://plugins.etl.linkedpipes.com/resource/l-DcatAp11ToCkan/profiles/CKAN");

        try (final TestEnvironment env = TestEnvironment.create(component, TestUtils.getTempDirectory())) {
            TestUtils.load(env.bindSingleGraphDataUnit("Metadata"),
                    TestUtils.fileFromResource("input.ttl"), RDFFormat.TURTLE);
            TestUtils.load(env.bindSingleGraphDataUnit("Codelists"),
                    TestUtils.fileFromResource("filetypes-skos.ttl"), RDFFormat.TURTLE);

            env.execute();
        } catch (Exception ex) {
            LOG.error("Failure", ex);
        }
    }

}
