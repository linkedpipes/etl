package com.linkedpipes.plugin.extractor.sparql.endpoint;

import org.eclipse.rdf4j.http.client.SPARQLProtocolSession;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TolerantSparqlRepository extends SPARQLRepository {

    static class TolerantValueFactory extends SimpleValueFactory {

        private final ValueFactory valueFactory;

        public TolerantValueFactory(ValueFactory valueFactory) {
            this.valueFactory = valueFactory;
        }

        @Override
        public Literal createLiteral(String value, IRI datatype) {
            if (RDF.LANGSTRING.equals(datatype)) {
                LOG.warn("Missing language tag for '{}'.", value);
                return valueFactory.createLiteral(value, XMLSchema.STRING);
            } else {
                return valueFactory.createLiteral(value, datatype);
            }
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(TolerantSparqlRepository.class);

    private ValueFactory valueFactory;

    private final ParserConfig config = new ParserConfig();

    public TolerantSparqlRepository(String endpointUrl) {
        super(endpointUrl);
        this.valueFactory = super.getValueFactory();
    }

    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    @Override
    protected SPARQLProtocolSession createHTTPClient() {
        SPARQLProtocolSession httpClient = super.createHTTPClient();
        httpClient.setValueFactory(getValueFactory());
        httpClient.setParserConfig(config);
        return httpClient;
    }

    public void fixMissingLanguageTag() {
        this.valueFactory = new TolerantValueFactory(this.valueFactory);
    }

    public void ignoreInvalidData() {
        config.addNonFatalError(BasicParserSettings.VERIFY_URI_SYNTAX);
        config.addNonFatalError(BasicParserSettings.VERIFY_RELATIVE_URIS);
    }

}
