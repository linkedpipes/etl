package com.linkedpipes.plugin.extractor.httpget;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import com.linkedpipes.plugin.ehttpgetfile.main.HttpGetFileConfiguration;
import org.junit.jupiter.api.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(HttpGetFileConfiguration.class);
    }

}
