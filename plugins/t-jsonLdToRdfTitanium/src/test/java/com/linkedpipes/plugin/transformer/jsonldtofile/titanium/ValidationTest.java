package com.linkedpipes.plugin.transformer.jsonldtofile.titanium;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import org.junit.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        (new TestConfigurationDescription())
                .test(JsonLdToRdfTitaniumConfiguration.class);
    }

}
