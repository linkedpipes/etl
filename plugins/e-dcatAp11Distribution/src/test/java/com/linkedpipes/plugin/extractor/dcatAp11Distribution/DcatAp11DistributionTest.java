package com.linkedpipes.plugin.extractor.dcatAp11Distribution;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import com.sun.deploy.resources.ResourceManager;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.plugin.extractor.dcatAp11Distribution.DcatAp11DistributionConfig.LocalizedString;
import com.linkedpipes.etl.component.test.TestEnvironment;
import com.linkedpipes.etl.component.test.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Klímek Jakub
 */
public class DcatAp11DistributionTest {

    private static final Logger LOG = LoggerFactory.getLogger(DcatAp11DistributionTest.class);

    @Test
    public void fullMetadata() throws Exception {
        final DcatAp11Distribution component = new DcatAp11Distribution();
        component.configuration = new DcatAp11DistributionConfig();

        try (final TestEnvironment env = TestEnvironment.create(component, TestUtils.getTempDirectory())) {
            
            final WritableSingleGraphDataUnit metadata = env.bindSingleGraphDataUnit("Metadata");
			final WritableSingleGraphDataUnit dataset = env.bindSingleGraphDataUnit("Dataset");

			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("data.ttl").getFile());
			RepositoryConnection conn = dataset.getRepository().getConnection();
            conn.add(file, "http://should.not.exist", RDFFormat.TURTLE);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            
            component.configuration.setGetDatasetIRIFromInput(true);
            //component.configuration.setDatasetIRI("http://my.dataset.iri");
            component.configuration.setGenDistroIRI(true);
			//component.configuration.setDistributionIRI("http://my.distro.iri");
			component.configuration.setAccessURLs(Arrays.asList(
					"http://my.access.url1",
					"http://my.access.url2"
			));
			component.configuration.setFormatIRI("http://publications.europa.eu/resource/authority/file-type/ATOM");
			component.configuration.setLicenseIRI("http://my.license");
            component.configuration.setDescriptions(Arrays.asList(
            		new LocalizedString("český popis","cs"),
            		new LocalizedString("english description","en"),
            		new LocalizedString("description en español","es")
            		));
			component.configuration.setDownloadURLs(Arrays.asList(
					"http://download1.cz",
					"http://download2.cz"
			));
			component.configuration.setMediaType("text/turtle");
			component.configuration.setTitles(Arrays.asList(
					new LocalizedString("český název","cs"),
					new LocalizedString("english title","en"),
					new LocalizedString("titulo en español","es")
			));
			component.configuration.setDocumentationIRIs(Arrays.asList(
					"http://etl.linkedpipes.com",
					"http://linkedpipes.com"
			));
			component.configuration.setLanguagesFromDataset(false);
			component.configuration.setLanguages(Arrays.asList(
					"http://publications.europa.eu/resource/authority/language/CES",
					"http://publications.europa.eu/resource/authority/language/ENG"
			));
			component.configuration.setConformsToIRIs(Arrays.asList(
					"http://etl.linkedpipes.com/schema1",
					"http://etl.linkedpipes.com/schema2"
			));
	        component.configuration.setStatusIRI("http://purl.org/adms/status/UnderDevelopment");
			component.configuration.setIssuedFromDataset(false);
			component.configuration.setIssued(sdf.parse("2016-06-24"));
			component.configuration.setModifiedFromDataset(false);
			component.configuration.setModifiedNow(false);
	        component.configuration.setModified(sdf.parse("2016-06-25"));
	        component.configuration.setRightsIRI("http://etl.linkedpipes.com/rights");

	        component.configuration.setByteSize(42);
            component.configuration.setChecksum("deadbeef");

			component.configuration.setSpatialIRIs(Arrays.asList(
					"http://publications.europa.eu/resource/authority/place/CZE_PRG",
					"http://ruian.linked.opendata.cz/resource/staty/1"
			));
			component.configuration.setTemporalStart(sdf.parse("2010-01-01"));
			component.configuration.setTemporalEnd(sdf.parse("2020-12-31"));

			env.execute();
            TestUtils.store(metadata, new File("metadata.ttl"), RDFFormat.TURTLE);
            conn.close();
        } catch (Exception ex) {
            LOG.error("Failure", ex);
        }
    }

}
