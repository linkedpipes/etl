package com.linkedpipes.plugin.extractor.oauth2token;

import com.linkedpipes.etl.test.TestUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * The plugin test suite does not check config:private, while the secrecy
 * of the client secret in exports depends on it.
 */
public class ConfigurationDescriptionPrivacyTest {

    private static final String IS_PRIVATE =
            "http://plugins.linkedpipes.com/ontology/configuration/private";

    private static final String MEMBER_PREFIX =
            "http://linkedpipes.com/resources/components/e-oauth2Token"
                    + "/0.0.0/configuration/desc/";

    @Test
    public void clientSecretIsPrivate() throws Exception {
        Assertions.assertEquals(Boolean.TRUE, isPrivate("clientSecret"));
    }

    @Test
    public void otherMembersAreNotPrivate() throws Exception {
        for (String member : List.of(
                "provider", "tenant", "clientId", "scope", "tokenEndpoint")) {
            Assertions.assertNotEquals(Boolean.TRUE, isPrivate(member),
                    member + " must stay public");
        }
    }

    private Boolean isPrivate(String member) throws Exception {
        var valueFactory = SimpleValueFactory.getInstance();
        var subject = valueFactory.createIRI(MEMBER_PREFIX + member);
        var predicate = valueFactory.createIRI(IS_PRIVATE);
        Boolean result = null;
        for (Statement statement : TestUtils.statementsFromResource(
                "LP-ETL/template/config-desc.ttl")) {
            if (!subject.equals(statement.getSubject())
                    || !predicate.equals(statement.getPredicate())) {
                continue;
            }
            Value value = statement.getObject();
            Assertions.assertTrue(value.isLiteral(),
                    "config:private must be a literal");
            Assertions.assertNull(result,
                    "duplicate config:private for " + member);
            result = Boolean.valueOf(value.stringValue());
        }
        return result;
    }

}
