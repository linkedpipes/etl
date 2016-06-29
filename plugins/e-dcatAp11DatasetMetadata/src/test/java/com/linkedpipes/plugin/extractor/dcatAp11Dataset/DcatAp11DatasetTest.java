package com.linkedpipes.plugin.extractor.dcatAp11Dataset;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.plugin.extractor.dcatAp11Dataset.DcatAp11DatasetConfig.LocalizedString;
import com.linkedpipes.plugin.extractor.dcatAp11Dataset.DcatAp11DatasetConfig.Language;
import com.linkedpipes.etl.component.test.TestEnvironment;
import com.linkedpipes.etl.component.test.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Klímek Jakub
 */
public class DcatAp11DatasetTest {

    private static final Logger LOG = LoggerFactory.getLogger(DcatAp11DatasetTest.class);

    @Test
    public void fullMetadata() throws Exception {
        final DcatAp11Dataset component = new DcatAp11Dataset();
        component.configuration = new DcatAp11DatasetConfig();

        try (final TestEnvironment env = TestEnvironment.create(component, TestUtils.getTempDirectory())) {
            
            final WritableSingleGraphDataUnit metadata = env.bindSingleGraphDataUnit("Metadata");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            
            component.configuration.setDatasetIRI("http://example.org/resource/dataset1");
            component.configuration.setTitles(Arrays.asList(
            		new LocalizedString("český název","cs"),
            		new LocalizedString("english title","en"),
            		new LocalizedString("titulo en español","es")
            		));
            component.configuration.setDescriptions(Arrays.asList(
            		new LocalizedString("český popis","cs"),
            		new LocalizedString("english description","en"),
            		new LocalizedString("description en español","es")
            		));
            component.configuration.setContactPointTypeIRI("http://www.w3.org/2006/vcard/ns#Individual");
            component.configuration.setContactPointName("Jane Doe");
            component.configuration.setContactPointEmail("jane@doe.org");
            component.configuration.setKeywords(Arrays.asList(
            		new LocalizedString("český kw1","cs"),
            		new LocalizedString("český kw2","cs"),
            		new LocalizedString("english kw","en"),
            		new LocalizedString("español kw","es")
            		));
            component.configuration.setEuThemeIRI("http://publications.europa.eu/resource/authority/data-theme/REGI");
	        component.configuration.setOtherThemeIRIs(Arrays.asList(
	        		"http://eurovoc.europa.eu/3437",
	        		"http://eurovoc.europa.eu/3450"
	        		));
	        component.configuration.setPublisherIRI("https://jakubklimek.com");
            component.configuration.setPublisherNames(Arrays.asList(
            		new LocalizedString("Česká organizace","cs"),
            		new LocalizedString("Czech organization","en")
            		));
            component.configuration.setPublisherTypeIRI("http://purl.org/adms/publishertype/NationalAuthority");
	        component.configuration.setLanguages(Arrays.asList(
	        		new Language("http://publications.europa.eu/resource/authority/language/CES"),
	        		new Language("http://publications.europa.eu/resource/authority/language/ENG")
	        		));
	        component.configuration.setAccrualPeriodicityIRI("http://publications.europa.eu/resource/authority/frequency/MONTHLY");
	        component.configuration.setIssued(sdf.parse("2016-06-24"));
	        component.configuration.setModified(sdf.parse("2016-06-25"));
	        component.configuration.setSpatialIRIs(Arrays.asList(
	        		"http://publications.europa.eu/resource/authority/place/CZE_PRG",
	        		"http://ruian.linked.opendata.cz/resource/staty/1"
	        		));
	        component.configuration.setTemporalStart(sdf.parse("2010-01-01"));
	        component.configuration.setTemporalEnd(sdf.parse("2020-12-31"));
	        component.configuration.setDocumentationIRIs(Arrays.asList(
	        		"http://etl.linkedpipes.com",
	        		"http://linkedpipes.com"
	        		));
	        component.configuration.setAccessRightsIRI("http://etl.linkedpipes.com/accessRights");
	        component.configuration.setIdentifier("http://etl.linkedpipes.com/datasetIdentifier or other");
	        component.configuration.setDatasetTypeIRI("http://publications.europa.eu/resource/authority/dataset-type/STATISTICAL");
            component.configuration.setProvenance(Arrays.asList(
            		new LocalizedString("Rodokmen česky","cs"),
            		new LocalizedString("Dataset lineage description","en")
            		));

	        component.configuration.setSampleIRIs(Arrays.asList(
	        		"http://etl.linkedpipes.com/sample1",
	        		"http://etl.linkedpipes.com/sample2"
	        		));
	        component.configuration.setLandingPageIRIs(Arrays.asList(
	        		"http://etl.linkedpipes.com/landingpage1",
	        		"http://etl.linkedpipes.com/landingpage2"
	        		));
	        component.configuration.setRelatedIRIs(Arrays.asList(
	        		"http://etl.linkedpipes.com/related1",
	        		"http://etl.linkedpipes.com/related2"
	        		));
	        component.configuration.setConfromsToIRIs(Arrays.asList(
	        		"http://etl.linkedpipes.com/conformsTo1",
	        		"http://etl.linkedpipes.com/conformsTo2"
	        		));
	        component.configuration.setSourceIRIs(Arrays.asList(
	        		"http://etl.linkedpipes.com/source1",
	        		"http://etl.linkedpipes.com/source2"
	        		));
	        component.configuration.setHasVersionIRIs(Arrays.asList(
	        		"http://etl.linkedpipes.com/version1",
	        		"http://etl.linkedpipes.com/version2"
	        		));
	        component.configuration.setIsVersionOfIRIs(Arrays.asList(
	        		"http://etl.linkedpipes.com/version4",
	        		"http://etl.linkedpipes.com/version5"
	        		));
	        component.configuration.setVersion("0.1");
            component.configuration.setVersionNotes(Arrays.asList(
            		new LocalizedString("České poznámky","cs"),
            		new LocalizedString("English version notes","en")
            		));

            env.execute();
            TestUtils.store(metadata, new File("metadata.ttl"), RDFFormat.TURTLE);
        } catch (Exception ex) {
            LOG.error("Failure", ex);
        }
    }

}
