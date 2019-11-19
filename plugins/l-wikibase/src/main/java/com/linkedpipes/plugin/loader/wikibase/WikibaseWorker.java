package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.plugin.loader.wikibase.model.DocumentMerger;
import com.linkedpipes.plugin.loader.wikibase.model.MergeStrategy;
import com.linkedpipes.plugin.loader.wikibase.model.RdfToDocument;
import com.linkedpipes.plugin.loader.wikibase.model.SnakEqual;
import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.rdf.PropertyRegister;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.LoginFailedException;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataEditor;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

class WikibaseWorker implements TaskConsumer<WikibaseTask> {

    @FunctionalInterface
    private interface Action<T> {

        T apply() throws IOException, MediaWikiApiErrorException;

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(WikibaseWorker.class);

    private final WikibaseLoaderConfiguration configuration;

    private final ExceptionFactory exceptionFactory;

    private final List<Statement> source;

    private final WritableSingleGraphDataUnit outputRdf;

    private ApiConnection connection;

    private WikibaseDataEditor wbde;

    private WikibaseDataFetcher wbdf;

    private PropertyRegister register;

    private Throwable lastException;

    public WikibaseWorker(
            WikibaseLoaderConfiguration configuration,
            ExceptionFactory exceptionFactory,
            WritableSingleGraphDataUnit outputRdf,
            List<Statement> source) {
        this.configuration = configuration;
        this.exceptionFactory = exceptionFactory;
        this.outputRdf = outputRdf;
        this.source = source;
    }

    public void onBeforeExecution() throws LpException {
        initializeConnection();
        loadPropertyRegister();
        //
        wbde = new WikibaseDataEditor(
                connection, configuration.getSiteIri() + "entity/");
        wbde.setEditAsBot(true);
        wbdf = new WikibaseDataFetcher(
                connection, configuration.getSiteIri() + "entity/");
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

    private void loadPropertyRegister() {
        register = new PropertyRegister(
                configuration.getRefProperty(),
                connection,
                configuration.getSiteIri() + "entity/");
        register.fetchUsingSPARQL(URI.create(configuration.getSparqlUrl()));
    }

    @Override
    public void accept(WikibaseTask task) throws LpException {
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, configuration.getSiteIri());
        LOG.debug("Processing: {}", task.getIri());
        try {
            ItemDocument local =
                    rdfToDocument.loadItemDocument(source, task.getIri());
            ItemDocument remote = null;
            Map<Object, MergeStrategy> mergeStrategy =
                    rdfToDocument.getMergeStrategyForLastLoadedDocument();
            MergeStrategy documentStrategy = mergeStrategy.get(local);
            switch (documentStrategy) {
                case NEW:
                    remote = onNew(local);
                    emitMapping(task, remote);
                    break;
                case DELETE:
                    onDelete(local);
                    break;
                case REPLACE:
                    remote = onReplace(local);
                    break;
                case MERGE:
                    remote = onMerge(local, mergeStrategy);
                    break;
                default:
                    throw new RuntimeException(
                            "Unsupported strategy:" + documentStrategy);
            }
            if (remote != null) {
                LOG.info(
                        "Output document: {} version: {}",
                        local.getEntityId(),
                        remote.getRevisionId());
            }
        } catch (Throwable ex) {
            lastException = ex;
            throw exceptionFactory.failure(
                    "Error processing document: {}", task.getIri(), ex);
        }
    }

    private ItemDocument onNew(ItemDocument local)
            throws IOException, MediaWikiApiErrorException {
        return execute(() -> wbde.createItemDocument(
                local, configuration.getNewItemMessage(), null));
    }

    private <T> T execute(Action<T> action)
            throws IOException, MediaWikiApiErrorException {
        int tryCounter = 0;
        while (true) {
            IOException lastException = null;
            try {
                return action.apply();
            } catch (IOException ex) {
                lastException = ex;
            }
            tryCounter++;
            if (tryCounter >= configuration.getRetryCount()) {
                throw lastException;
            }
            LOG.warn("Operation failed, waiting before retry", lastException);
            try {
                Thread.sleep(configuration.getRetryWait());
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }
    }

    private void onDelete(ItemDocument local) {
        throw new RuntimeException("Entity delete is not supported!");
    }

    private ItemDocument onReplace(ItemDocument local)
            throws IOException, MediaWikiApiErrorException {
        return execute(() -> wbde.editItemDocument(
                local, true, configuration.getReplaceItemMessage(), null));
    }

    private ItemDocument onMerge(
            ItemDocument local, Map<Object, MergeStrategy> mergeStrategy)
            throws IOException, MediaWikiApiErrorException {
        ItemDocument remote =
                (ItemDocument) wbdf.getEntityDocument(
                        local.getEntityId().getId());
        DocumentMerger merger =
                new DocumentMerger(
                        local, remote, mergeStrategy, createSnakEqual());
        if (merger.canUpdateExisting()) {
            ItemDocument newDocument = merger.assembleMergeDocument();
            return execute(() -> wbde.editItemDocument(
                    newDocument,
                    false, configuration.getMergeItemMessage(), null));
        } else {
            ItemDocument newDocument = merger.assembleReplaceDocument();
            return execute(() -> wbde.editItemDocument(
                    newDocument,
                    true, configuration.getMergeByReplaceItemMessage(), null));
        }
    }

    private SnakEqual createSnakEqual() {
        if (configuration.isStrictMatching()) {
            return SnakEqual.strict();
        } else {
            return SnakEqual.relaxed();
        }
    }

    private void emitMapping(WikibaseTask task, ItemDocument document)
            throws RdfException {
        TripleWriter writer = outputRdf.getWriter();
        writer.iri(
                task.getIri(),
                WikibaseLoaderVocabulary.WIKIDATA_MAPPING,
                document.getEntityId().getIri());
        writer.flush();
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

    public Throwable getLastException() {
        return lastException;
    }

}
