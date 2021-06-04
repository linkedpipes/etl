package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import com.linkedpipes.plugin.transformer.sparql.constructtofilelist.SparqlConstructToFileListConfiguration;
import org.junit.jupiter.api.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(SparqlConstructToFileListConfiguration.class);
    }

}
