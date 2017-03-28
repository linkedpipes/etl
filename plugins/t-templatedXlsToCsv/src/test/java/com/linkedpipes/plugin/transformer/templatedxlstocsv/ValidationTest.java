package com.linkedpipes.plugin.transformer.templatedxlstocsv;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import org.junit.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(XlsToCsvConfiguration.class);
    }

}
