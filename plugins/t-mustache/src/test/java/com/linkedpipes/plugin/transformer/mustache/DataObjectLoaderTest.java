package com.linkedpipes.plugin.transformer.mustache;

import com.linkedpipes.etl.test.TestUtils;
import org.eclipse.rdf4j.model.Statement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class DataObjectLoaderTest {

    static class DataObjectLoaderMock extends DataObjectLoader {

        protected List<Statement> statements;

        public DataObjectLoaderMock(
                MustacheConfiguration configuration,
                List<Statement> statements) {
            super(configuration);
            this.statements = statements;
        }

        @Override
        protected void parseStatements() {
            for (Statement statement : statements) {
                parseStatement(statement);
            }
        }
    }

    /**
     * Cycle.
     */
    @Test
    public void issue859() throws Exception {
        var input = TestUtils.statementsFromResource("issue-859.ttl");
        MustacheConfiguration configuration = new MustacheConfiguration();
        configuration.setAddFirstToCollection(true);
        configuration.setResourceClass("urn:Root");
        DataObjectLoaderMock loader =
                new DataObjectLoaderMock(configuration, input);
        var actual = loader.loadData(null, null);
        Assertions.assertEquals(1, actual.size());
        var root = (Map) actual.iterator().next().data;
        Assertions.assertEquals(
                "urn:root", root.get("@id"));
        var type = (Map) root.get(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Assertions.assertEquals(
                "urn:Root", type.get("@id"));
        var dataset = (Map) root.get("urn:dataset");
        Assertions.assertNotNull(dataset);
        Assertions.assertEquals(
                true, dataset.get(MustacheVocabulary.HAS_IS_FIRST));
        var keyword = (Map) dataset.get("urn:keywords_en");
        Assertions.assertNotNull(keyword);
        Assertions.assertEquals(
                true, keyword.get(MustacheVocabulary.HAS_IS_FIRST));
    }

    /**
     * Detection of first in an array with cycle.
     */
    @Test
    public void issue866() throws Exception {
        var input = TestUtils.statementsFromResource("issue-866.ttl");
        MustacheConfiguration configuration = new MustacheConfiguration();
        configuration.setAddFirstToCollection(true);
        configuration.setResourceClass("urn:Root");
        DataObjectLoaderMock loader =
                new DataObjectLoaderMock(configuration, input);
        var actual = loader.loadData(null, null);
        Assertions.assertEquals(1, actual.size());
        var root = (Map) actual.iterator().next().data;
        Assertions.assertEquals(
                "urn:root", root.get("@id"));
        var datasets = (List) root.get("urn:dataset");
        Assertions.assertNotNull(datasets);
        Assertions.assertEquals(2, datasets.size());
        var dataset1 = (Map) datasets.get(0);
        Assertions.assertEquals(
                true, dataset1.get(MustacheVocabulary.HAS_IS_FIRST));
        var dataset2 = (Map) datasets.get(1);
        Assertions.assertEquals(
                false, dataset2.get(MustacheVocabulary.HAS_IS_FIRST));
        var dataset1s = (List) dataset1.get("urn:dataset");
        Assertions.assertEquals(2, dataset1s.size());
        var dataset11 = (Map) dataset1s.get(0);
        Assertions.assertEquals(
                true, dataset11.get(MustacheVocabulary.HAS_IS_FIRST));
        var dataset12 = (Map) dataset1s.get(1);
        Assertions.assertEquals(
                false, dataset12.get(MustacheVocabulary.HAS_IS_FIRST));
        var resource = (Map) root.get("urn:resource");
        Assertions.assertNotNull(resource);
        Assertions.assertEquals(
                true, resource.get(MustacheVocabulary.HAS_IS_FIRST));
    }

}
