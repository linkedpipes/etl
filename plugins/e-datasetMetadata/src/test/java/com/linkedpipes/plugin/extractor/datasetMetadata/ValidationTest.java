package com.linkedpipes.plugin.extractor.datasetMetadata;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import org.junit.jupiter.api.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        (new TestConfigurationDescription()).test(DatasetMetadataConfig.class);
    }

}
