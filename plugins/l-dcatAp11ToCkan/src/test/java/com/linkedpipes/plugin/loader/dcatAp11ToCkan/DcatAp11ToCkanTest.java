package com.linkedpipes.plugin.loader.dcatAp11ToCkan;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import com.linkedpipes.etl.component.test.TestEnvironment;
import com.linkedpipes.etl.component.test.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Klímek Jakub
 */
public class dcatAp11ToCkanTest {

    private static final Logger LOG = LoggerFactory.getLogger(dcatAp11ToCkanTest.class);

    //@Test
    public void loadTest() throws Exception {
        final dcatAp11ToCkan component = new dcatAp11ToCkan();
        component.configuration = new dcatAp11ToCkanConfiguration();
        component.configuration.setApiUri("");
        component.configuration.setApiKey("");
        component.configuration.setLoadLanguage("cs");
        component.configuration.setDatasetID("test9999");
        component.configuration.setProfile("http://plugins.etl.linkedpipes.com/resource/l-dcatAp11ToCkan/profiles/CKAN");

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
