package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import org.eclipse.rdf4j.http.client.SPARQLProtocolSession;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TolerantSparqlRepository extends SPARQLRepository {

    class TolerantValueFactory extends SimpleValueFactory {

        @Override
        public Literal createLiteral(String value, IRI datatype) {
            if (RDF.LANGSTRING.equals(datatype)) {
                LOG.warn("Missing language tag for '%s'.", value);
                return simpleFactory.createLiteral(value, XMLSchema.STRING);
            } else {
                return simpleFactory.createLiteral(value, datatype);
            }
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(TolerantSparqlRepository.class);

    private final ValueFactory simpleFactory =
            SimpleValueFactory.getInstance();

    private final ValueFactory updatedFactory =
            new TolerantValueFactory();

    public TolerantSparqlRepository(String endpointUrl) {
        super(endpointUrl);
    }

    @Override
    public ValueFactory getValueFactory() {
        return updatedFactory;
    }

    @Override
    protected SPARQLProtocolSession createHTTPClient() {
        SPARQLProtocolSession httpClient = super.createHTTPClient();
        httpClient.setValueFactory(getValueFactory());
        return httpClient;
    }

}
