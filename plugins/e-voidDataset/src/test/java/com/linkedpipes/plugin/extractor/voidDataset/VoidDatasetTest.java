package com.linkedpipes.plugin.extractor.voidDataset;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.plugin.extractor.voidDataset.VoidDatasetConfig.LocalizedString;
import com.linkedpipes.etl.component.test.TestEnvironment;
import com.linkedpipes.etl.component.test.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Klímek Jakub
 */
public class VoidDatasetTest {

    private static final Logger LOG = LoggerFactory.getLogger(VoidDatasetTest.class);

    //@Test
    public void fullMetadata() throws Exception {
        final VoidDataset component = new VoidDataset();
        component.configuration = new VoidDatasetConfig();

        try (final TestEnvironment env = TestEnvironment.create(component, TestUtils.getTempDirectory())) {
            
            final WritableSingleGraphDataUnit metadata = env.bindSingleGraphDataUnit("Metadata");
			final WritableSingleGraphDataUnit distribution = env.bindSingleGraphDataUnit("Distribution");

			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("data.ttl").getFile());
			RepositoryConnection conn = distribution.getRepository().getConnection();
            conn.add(file, "http://should.not.exist", RDFFormat.TURTLE);

            component.configuration.setGetDistributionIRIFromInput(true);
			component.configuration.setExampleResourceIRIs(Arrays.asList(
					"http://my.example.resource.url1",
					"http://my.example.resource.url2"
			));
			component.configuration.setSparqlEndpointIRI("http://linked.opendata.cz/sparql");

			env.execute();
//            TestUtils.store(metadata, new File("metadata.ttl"), RDFFormat.TURTLE);
            conn.close();
        } catch (Exception ex) {
            LOG.error("Failure", ex);
        }
    }

}
