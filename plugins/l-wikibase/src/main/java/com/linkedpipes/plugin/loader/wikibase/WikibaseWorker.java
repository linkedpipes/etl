package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.vocabulary.RDF;
import com.linkedpipes.etl.executor.api.v1.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.LoginFailedException;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;

class WikibaseWorker implements TaskConsumer<WikibaseTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(WikibaseWorker.class);

    private final WikibaseLoaderConfiguration configuration;

    private final ExceptionFactory exceptionFactory;

    private final RdfSource source;

    private final WritableSingleGraphDataUnit outputRdf;

    private ApiConnection connection;

    private DocumentSynchronizer synchronizer;

    public WikibaseWorker(
            WikibaseLoaderConfiguration configuration,
            ExceptionFactory exceptionFactory,
            WritableSingleGraphDataUnit outputRdf,
            RdfSource source) {
        this.configuration = configuration;
        this.exceptionFactory = exceptionFactory;
        this.outputRdf = outputRdf;
        this.source = source;
    }

    public void onBeforeExecution() throws LpException {
        initializeConnection();
        createSynchronizer();
    }

    private void initializeConnection() throws LpException {
        connection = new ApiConnection(configuration.getEndpoint());
        try {
            connection.login(
                    configuration.getUserName(),
                    configuration.getPassword());
        } catch (LoginFailedException | NullPointerException ex) {
            throw exceptionFactory.failure("Can't login.", ex);
        }
    }

    private void createSynchronizer() {
        this.synchronizer = new DocumentSynchronizer(
                exceptionFactory, connection,
                configuration.getSiteIri(),
                outputRdf,
                configuration.getAverageTimePerEdit());
    }

    @Override
    public void accept(WikibaseTask task) throws LpException {
        WikibaseDocument document = loadDocument(task);
        LOG.debug("Processing: {}", task.getIri());
        try {
            synchronizer.synchronize(document);
        } catch (MediaWikiApiErrorException | IOException ex) {
            throw exceptionFactory.failure(
                    "Error processing document: {}",
                    document.getIri(), ex);
        }
    }

    private WikibaseDocument loadDocument(WikibaseTask task)
            throws RdfException {
        String statementPropertyPrefix =
                configuration.getSiteIri() + "prop/";
        String iri = task.getIri();
        WikibaseDocument document = new WikibaseDocument(iri);
        source.statements(iri, (predicate, value) -> {
            if (RDF.TYPE.equals(predicate)) {
                document.getTypes().add(value.asString());
            } else if (isLabel(predicate)) {
                document.setLabel(value.asString(), value.getLanguage());
            } else if (predicate.startsWith(statementPropertyPrefix)) {
                if (value.getType() != null) {
                    // It is not IRI.
                    return;
                }
                WikibaseDocument.Statement statement =
                        loadStatement(source, value, predicate);
                if (statement != null) {
                    document.addStatement(statement);
                }
            }
        });
        return document;
    }

    private boolean isLabel(String predicate) {
        return SKOS.PREF_LABEL.equals(predicate) ||
                RDFS.LABEL.toString().equals(predicate);
    }

    /**
     * Return null if the given entity is not Wikidata statement.
     */
    private WikibaseDocument.Statement loadStatement(
            RdfSource source, RdfValue statementIri, String statementPredicate)
            throws RdfException {
        WikibaseDocument.Statement statement =
                new WikibaseDocument.Statement(
                        statementIri.asString(), statementPredicate);
        String valuePredicate = configuration.getSiteIri() +
                "prop/statement/" + statement.getPredicate();
        source.statements(statementIri.asString(), (predicate, value) -> {
            if (RDF.TYPE.equals(predicate)) {
                statement.getTypes().add(value.asString());
            } else if (valuePredicate.equals(predicate)) {
                statement.setValue(value.asString());
            }
        });
        if (isStatement(statement)) {
            return statement;
        } else {
            return null;
        }
    }

    private boolean isStatement(WikibaseDocument.Statement statement) {
        return statement.getTypes().contains(
                WikibaseLoaderVocabulary.WIKIDATA_STATEMENT) ||
                statement.getTypes().contains(
                        WikibaseLoaderVocabulary.WIKIDATA_NEW_ENTITY);
    }

    public void onAfterExecution() throws LpException {
        closeConnection();
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

}
