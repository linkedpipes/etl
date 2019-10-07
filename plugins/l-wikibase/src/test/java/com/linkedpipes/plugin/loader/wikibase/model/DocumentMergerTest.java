package com.linkedpipes.plugin.loader.wikibase.model;

import org.junit.Assert;
import org.junit.Test;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.ReferenceBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;

import java.util.HashMap;
import java.util.Map;

public class DocumentMergerTest {

    /**
     * Merge two entities with same statement but different references.
     */
    @Test
    public void mergeWithDefaultStrategy() {
        Map<Object, MergeStrategy> strategy = new HashMap<>();
        String siteIri = "ttp://www.wikidata.org/entity/";
        ItemIdValue id = Datamodel.makeItemIdValue("Q10", siteIri);
        ItemDocumentBuilder local = ItemDocumentBuilder.forItemId(id);
        local.withLabel("Name", "en");
        local.withDescription("Description", "en");
        StatementBuilder localBuilder = StatementBuilder.forSubjectAndProperty(
                id, Datamodel.makePropertyIdValue("P10", siteIri));
        localBuilder.withValue(Datamodel.makeStringValue("value"));
        localBuilder.withId("Q10$123");
        localBuilder.withReference(
                ReferenceBuilder.newInstance()
                        .withPropertyValue(
                                Datamodel.makePropertyIdValue("P3", siteIri),
                                Datamodel.makeStringValue("Reference #1"))
                        .withPropertyValue(
                                Datamodel.makePropertyIdValue("P3", siteIri),
                                Datamodel.makeStringValue("Reference #2"))
                        .build());
        local.withStatement(localBuilder.build());
        //
        ItemDocumentBuilder remote = ItemDocumentBuilder.forItemId(id);
        remote.withLabel("Jmeno", "cs");
        remote.withDescription("Popis", "cs");
        remote.withDescription("Description", "en");
        StatementBuilder remoteBuilder = StatementBuilder.forSubjectAndProperty(
                id, Datamodel.makePropertyIdValue("P10", siteIri));
        remoteBuilder.withValue(Datamodel.makeStringValue("value"));
        remoteBuilder.withId("Q10$123");
        remoteBuilder.withReference(
                ReferenceBuilder.newInstance()
                        .withPropertyValue(
                                Datamodel.makePropertyIdValue("P3", siteIri),
                                Datamodel.makeStringValue("Reference #1"))
                        .build());
        remote.withStatement(remoteBuilder.build());
        // New document that should be added to the local data.
        ItemDocumentBuilder expected = ItemDocumentBuilder.forItemId(id);
        expected.withLabel("Name", "en");
        StatementBuilder expectedBuilder =
                StatementBuilder.forSubjectAndProperty(
                        id, Datamodel.makePropertyIdValue("P10", siteIri));
        expectedBuilder.withValue(Datamodel.makeStringValue("value"));
        expectedBuilder.withId("Q10$123");
        expectedBuilder.withReference(
                ReferenceBuilder.newInstance()
                        .withPropertyValue(
                                Datamodel.makePropertyIdValue("P3", siteIri),
                                Datamodel.makeStringValue("Reference #1"))
                        .withPropertyValue(
                                Datamodel.makePropertyIdValue("P3", siteIri),
                                Datamodel.makeStringValue("Reference #2"))
                        .build());
        expected.withStatement(expectedBuilder.build());
        // With no strategy, ie. merge by default we should get merged results.
        DocumentMerger merger = new DocumentMerger(
                local.build(), remote.build(), strategy, SnakEqual.strict());
        Assert.assertTrue(merger.canUpdateExisting());
        ItemDocument actual = merger.assembleMergeDocument();
        Assert.assertEquals(expected.build(), actual);
    }

}


