package com.linkedpipes.plugin.transformer.mustache;

import com.linkedpipes.etl.test.TestUtils;
import org.eclipse.rdf4j.model.Statement;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    }


}
