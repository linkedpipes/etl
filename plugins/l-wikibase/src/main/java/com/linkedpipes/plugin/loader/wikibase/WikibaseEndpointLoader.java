package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.vocabulary.RDF;
import com.linkedpipes.etl.executor.api.v1.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.LoginFailedException;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WikibaseEndpointLoader implements Component, SequentialExecution {

    public static final String WIKIDATA_ENTITY =
            "http://localhost/WikidataEntity";

    public static final String WIKIDATA_QID =
            "http://localhost/qid";

    public static final String SKOS_LABEL = SKOS.PREF_LABEL;

    public static final String RDFS_LABEL = RDFS.LABEL.toString();

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.Configuration
    public WikibaseLoaderConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private ApiConnection connection;

    @Override
    public void execute() throws LpException {
        initializeConnection();
        List<WikibaseDocument> documents = loadDocuments();
        DocumentSynchronizer synchronize = new DocumentSynchronizer(
                exceptionFactory, connection, configuration.getSiteIri());
        for (WikibaseDocument document : documents) {
            try {
                synchronize.synchronize(document);
            } catch (MediaWikiApiErrorException | IOException ex) {
                throw exceptionFactory.failure(
                        "Error processing document: {}",
                        document.getQid(), ex);
            }
        }
        closeConnection();
    }

    private void initializeConnection() throws LpException {
        connection = new ApiConnection(configuration.getEndpoint());
        try {
            connection.login(
                    configuration.getUserName(),
                    configuration.getPassword());
        } catch (LoginFailedException ex) {
            throw exceptionFactory.failure("Can't login.", ex);
        }
    }

    private void closeConnection() throws LpException {
        if (connection == null) {
            return;
        }
        try {
            connection.logout();
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't close connection.", ex);
        }
    }

    private List<WikibaseDocument> loadDocuments() throws RdfException {
        RdfSource source = inputRdf.asRdfSource();
        List<String> refs = source.getByType(WIKIDATA_ENTITY);
        List<WikibaseDocument> documents = new ArrayList<>(refs.size());
        for (String iri : refs) {
            WikibaseDocument document = new WikibaseDocument();
            source.statements(iri, (predicate, value) -> {
                if (WIKIDATA_QID.equals(predicate)) {
                    document.setQid(value.asString());
                } else if (RDF.TYPE.equals(predicate)) {
                    document.getTypes().add(value.asString());
                } else if (isLabel(predicate)) {
                    document.setLabel(value.asString(), value.getLanguage());
                } else {
                    document.addStatement(predicate, value.asString());
                }
            });
            documents.add(document);
        }
        return documents;
    }

    private boolean isLabel(String predicate) {
        return SKOS_LABEL.equals(predicate) || RDFS_LABEL.equals(predicate);
    }

}
