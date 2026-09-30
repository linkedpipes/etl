package com.linkedpipes.plugin.extractor.oauth2token;

import com.linkedpipes.etl.test.TestEnvironment;
import com.linkedpipes.etl.test.TestUtils;
import com.linkedpipes.etl.test.dataunit.TestSingleGraphDataUnit;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class OAuth2TokenTest {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/e-oauth2Token#";

    private static final String RESOURCE =
            "http://plugins.linkedpipes.com/resource/e-oauth2Token"
                    + "/accessToken";

    @Test
    public void tokenIsWrittenToOutput() throws Exception {
        List<Statement> statements = execute(
                "{\"access_token\":\"the-token\","
                        + "\"token_type\":\"Bearer\","
                        + "\"expires_in\":3599}");
        Assertions.assertEquals(4, statements.size());
        for (Statement statement : statements) {
            Assertions.assertEquals(
                    RESOURCE, statement.getSubject().stringValue());
        }
        Assertions.assertEquals("the-token",
                objectOf(statements, PREFIX + "accessToken"));
        Assertions.assertEquals("Bearer",
                objectOf(statements, PREFIX + "tokenType"));
        Assertions.assertEquals("3599",
                objectOf(statements, PREFIX + "expiresIn"));
        Assertions.assertEquals(PREFIX + "AccessToken", objectOf(statements,
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
    }

    @Test
    public void optionalMembersAreOmitted() throws Exception {
        Assertions.assertEquals(2,
                execute("{\"access_token\":\"the-token\"}").size());
    }

    @Test
    public void clientSecretIsNotWrittenToOutput() throws Exception {
        for (Statement statement
                : execute("{\"access_token\":\"the-token\"}")) {
            Assertions.assertFalse(
                    statement.toString().contains("client-secret"),
                    statement.toString());
        }
    }

    private List<Statement> execute(String responseBody) throws Exception {
        OAuth2Token component = new OAuth2Token();
        try (TestTokenServer server = new TestTokenServer(200, responseBody);
                TestEnvironment environment = TestEnvironment.create(
                        component, TestUtils.getTempDirectory())) {
            component.configuration =
                    TokenServiceTest.configuration(server.endpoint());
            TestSingleGraphDataUnit output =
                    environment.bindSingleGraphDataUnit("Output");
            environment.execute();
            return collect(output);
        }
    }

    private List<Statement> collect(TestSingleGraphDataUnit dataUnit) {
        List<Statement> result = new ArrayList<>();
        try (RepositoryConnection connection =
                     dataUnit.getRepository().getConnection()) {
            connection.getStatements(
                    null, null, null, dataUnit.getReadGraph())
                    .forEach(result::add);
        }
        return result;
    }

    private String objectOf(List<Statement> statements, String predicate) {
        for (Statement statement : statements) {
            if (predicate.equals(statement.getPredicate().stringValue())) {
                return statement.getObject().stringValue();
            }
        }
        return null;
    }

}
