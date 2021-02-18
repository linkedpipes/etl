package com.linkedpipes.plugin.transformer.jsonldformat;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import com.linkedpipes.plugin.transformer.jsonldformattitanium.JsonLdFormatTitaniumConfiguration;
import org.junit.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(JsonLdFormatTitaniumConfiguration.class);
    }

}
