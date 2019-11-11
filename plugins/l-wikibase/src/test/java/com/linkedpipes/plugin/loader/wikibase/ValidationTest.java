package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import org.junit.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        TestConfigurationDescription test = new TestConfigurationDescription();
        test.test(WikibaseLoaderConfiguration.class);
    }

}
