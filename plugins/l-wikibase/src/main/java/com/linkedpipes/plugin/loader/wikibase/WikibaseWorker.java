package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.plugin.loader.wikibase.model.DocumentsLoader;
import com.linkedpipes.plugin.loader.wikibase.model.Property;
import com.linkedpipes.plugin.loader.wikibase.model.WikibaseDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.LoginFailedException;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;
import java.util.Map;

class WikibaseWorker implements TaskConsumer<WikibaseTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(WikibaseWorker.class);

    private final WikibaseLoaderConfiguration configuration;

    private final ExceptionFactory exceptionFactory;

    private final RdfSource source;

    private final WritableSingleGraphDataUnit outputRdf;

    private ApiConnection connection;

    private DocumentSynchronizer synchronizer;

    private Exception lastException;

    private Map<String, Property> ontology;

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

    public void onBeforeExecution(
            Map<String, Property> ontology) throws LpException {
        this.ontology = ontology;
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
                configuration.getAverageTimePerEdit(),
                ontology);
    }

    @Override
    public void accept(WikibaseTask task) throws LpException {
        DocumentsLoader loader = new DocumentsLoader(
                configuration.getSiteIri(), source);
        WikibaseDocument document = loader.loadDocument(task.getIri());
        LOG.debug("Processing: {}", task.getIri());
        try {
            synchronizer.synchronize(document);
        } catch (MediaWikiApiErrorException | IOException ex) {
            lastException = ex;
            throw exceptionFactory.failure(
                    "Error processing document: {}",
                    document.getIri(), ex);
        }
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

    public Exception getLastException() {
        return lastException;
    }

}
