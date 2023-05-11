package com.linkedpipes.etl.library.template.reference.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsCompare;
import com.linkedpipes.etl.library.template.TestUtils;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class ReferenceTemplateAdapterTest {

    @Test
    public void toRdfAndBack() throws Exception {
        File file = TestUtils.file("reference/v5-eTextHolder.trig");
        Statements statements = Statements.arrayList();
        statements.file().addAll(file);
        List<RawReferenceTemplate> candidates =
                RdfToRawReferenceTemplate.asRawReferenceTemplates(
                        statements.selector());
        Assertions.assertEquals(1, candidates.size());
        var expected = candidates.get(0).toReferenceTemplate();

        var data = ReferenceTemplateToRdf.asRdf(expected);
        List<RawReferenceTemplate> actualCandidates =
                RdfToRawReferenceTemplate.asRawReferenceTemplates(
                        data.selector());
        Assertions.assertEquals(1, actualCandidates.size());
        var actual = actualCandidates.get(0).toReferenceTemplate();

        var expectedWithoutConfiguration = new ReferenceTemplate(
                expected.resource(), expected.version(), expected.template(),
                expected.plugin(),expected.label(), expected.description(),
                expected.note(), expected.color(), expected.tags(),
                expected.knownAs(), null, expected.configurationGraph());

        var actualWithoutConfiguration = new ReferenceTemplate(
                actual.resource(), actual.version(), actual.template(),
                actual.plugin(),actual.label(), actual.description(),
                actual.note(), actual.color(), actual.tags(),
                actual.knownAs(), null, actual.configurationGraph());

        Assertions.assertTrue(StatementsCompare.isIsomorphic(
                expected.configuration(),
                actual.configuration()));

        Assertions.assertEquals(
                expectedWithoutConfiguration, actualWithoutConfiguration);
    }

}
