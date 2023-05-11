package com.linkedpipes.plugin.ehttpgetfile.multiple;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import org.junit.jupiter.api.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        (new TestConfigurationDescription())
                .test(HttpGetFilesConfiguration.class, "multiple");
    }

}
