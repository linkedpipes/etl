package com.linkedpipes.plugin.extractor.oauth2token;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class OAuth2Token implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(OAuth2Token.class);

    private static final ValueFactory VALUE_FACTORY =
            SimpleValueFactory.getInstance();

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "Output")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public OAuth2TokenConfiguration configuration;

    @Override
    public void execute() throws LpException {
        LOG.info("Using provider: {}", configuration.getProvider());
        TokenProvider provider =
                TokenProvider.create(configuration.getProvider());
        TokenResponse token =
                (new TokenService(provider)).requestToken(configuration);
        writeOutput(token);
    }

    /**
     * Only the token response is written, so that no part of the
     * configuration can reach the output.
     */
    private void writeOutput(TokenResponse token) throws LpException {
        List<Statement> statements = createStatements(token);
        outputRdf.execute((connection) -> {
            connection.begin();
            connection.add(statements, outputRdf.getWriteGraph());
            connection.commit();
        });
    }

    private List<Statement> createStatements(TokenResponse token) {
        IRI resource = OAuth2TokenVocabulary.ACCESS_TOKEN_RESOURCE;
        List<Statement> result = new ArrayList<>(4);
        result.add(VALUE_FACTORY.createStatement(resource, RDF.TYPE,
                OAuth2TokenVocabulary.ACCESS_TOKEN_CLASS));
        result.add(VALUE_FACTORY.createStatement(resource,
                OAuth2TokenVocabulary.HAS_ACCESS_TOKEN,
                VALUE_FACTORY.createLiteral(token.accessToken())));
        if (token.tokenType() != null) {
            result.add(VALUE_FACTORY.createStatement(resource,
                    OAuth2TokenVocabulary.HAS_TOKEN_TYPE,
                    VALUE_FACTORY.createLiteral(token.tokenType())));
        }
        if (token.expiresIn() != null) {
            result.add(VALUE_FACTORY.createStatement(resource,
                    OAuth2TokenVocabulary.HAS_EXPIRES_IN,
                    VALUE_FACTORY.createLiteral(token.expiresIn())));
        }
        return result;
    }

}
