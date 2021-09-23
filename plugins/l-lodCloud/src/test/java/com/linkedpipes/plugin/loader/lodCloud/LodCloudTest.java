package com.linkedpipes.plugin.loader.lodCloud;

import com.linkedpipes.etl.test.TestEnvironment;
import com.linkedpipes.etl.test.TestUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LodCloudTest {

    private static final Logger LOG =
            LoggerFactory.getLogger(LodCloudTest.class);

    //@Test
    public void loadTest() throws Exception {
        final LodCloud component = new LodCloud();
        component.configuration = new LodCloudConfiguration();
        component.configuration.setApiKey("");
        component.configuration.setOrgID("9046f134-ea81-462f-aae3-69854d34fc96");
        component.configuration.setDatasetID("cz-test-4");
        component.configuration.setNamespace("https://gov.cz.linked.opendata.cz/zdroj/datová-schránka/");
        component.configuration.getVocabularies().add("skos");
        component.configuration.getVocabularies().add("schema");
        component.configuration.getLinks().add(new LodCloudConfiguration.LinkCount("cz-ruian", new Long(20000)));
        component.configuration.setVocabTag(LodCloudConfiguration.VocabTags.DerefVocab);
        component.configuration.setVocabMappingTag(LodCloudConfiguration.VocabMappingsTags.NoVocabMappings);
        component.configuration.setPublishedTag(LodCloudConfiguration.PublishedTags.PublishedByThirdParty);
        component.configuration.setProvenanceMetadataTag(LodCloudConfiguration.ProvenanceMetadataTags.NoProvenanceMetadata);
        component.configuration.setLicenseMetadataTag(LodCloudConfiguration.LicenseMetadataTags.LicenseMetadata);
        component.configuration.setLicense_id(LodCloudConfiguration.Licenses.cczero);

        try (final TestEnvironment env = TestEnvironment
                .create(component, TestUtils
                        .getTempDirectory())) {
            TestUtils.load(env.bindSingleGraphDataUnit("Metadata"),
                    TestUtils.fileFromResource("input.ttl"), RDFFormat.TURTLE);
            TestUtils.load(env.bindSingleGraphDataUnit("Codelists"),
                    TestUtils.fileFromResource("filetypes-skos.ttl"), RDFFormat.TURTLE);

            env.execute();
        } catch (Exception ex) {
            LOG.error("Failure", ex);
        }
    }

}
