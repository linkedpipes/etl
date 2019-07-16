package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
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

    public static final String WIKIDATA_NEW_ENTITY =
            "http://localhost/New";

    public static final String WIKIDATA_DELETE_ENTITY =
            "http://localhost/Remove";

    public static final String WIKIDATA_MAPPING =
            "http://localhost/mappedTp";

    public static final String SKOS_LABEL = SKOS.PREF_LABEL;

    public static final String RDFS_LABEL = RDFS.LABEL.toString();

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

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
                exceptionFactory, connection,
                configuration.getInstanceIriBase(),
                outputRdf,
                configuration.getAverageTimePerEdit());
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
        String propertyPrefix =
                configuration.getOntologyIriBase();
        String statementPrefix =
                configuration.getInstanceIriBase() + "statement/";

        RdfSource source = inputRdf.asRdfSource();
        List<String> refs = source.getByType(WIKIDATA_ENTITY);
        List<WikibaseDocument> documents = new ArrayList<>(refs.size());
        for (String iri : refs) {
            WikibaseDocument document = new WikibaseDocument(iri);
            source.statements(iri, (predicate, value) -> {
                if (RDF.TYPE.equals(predicate)) {
                    document.getTypes().add(value.asString());
                } else if (isLabel(predicate)) {
                    document.setLabel(value.asString(), value.getLanguage());
                } else if (predicate.startsWith(propertyPrefix)) {
                    if (value.getType() != null) {
                        // It is not IRI.
                        return;
                    }
                    WikibaseDocument.Statement statement =
                            loadStatement(source, value, predicate);
                    // Statement must be either new or have given prefix.
                    if (statement.isNew() ||
                            value.asString().startsWith(statementPrefix)) {
                        document.addStatement(statement);
                    }
                }
            });
            documents.add(document);
        }
        return documents;
    }

    private boolean isLabel(String predicate) {
        return SKOS_LABEL.equals(predicate) || RDFS_LABEL.equals(predicate);
    }

    private WikibaseDocument.Statement loadStatement(
            RdfSource source, RdfValue statementIri, String statementPredicate)
            throws RdfException {
        WikibaseDocument.Statement statement =
                new WikibaseDocument.Statement(
                        statementIri.asString(), statementPredicate);
        String valuePredicate = configuration.getOntologyIriBase() +
                "statement/" + statement.getPredicate();
        source.statements(statementIri.asString(), (predicate, value) -> {
            if (RDF.TYPE.equals(predicate)) {
                statement.getTypes().add(value.asString());
            } else if (valuePredicate.equals(predicate)) {
                statement.setValue(value.asString());
            }
        });
        return statement;
    }

}
