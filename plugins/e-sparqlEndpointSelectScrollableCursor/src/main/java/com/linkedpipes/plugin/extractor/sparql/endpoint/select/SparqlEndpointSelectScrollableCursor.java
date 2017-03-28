package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Use scrollable cursors to execute SPARQL select.
 */
public final class SparqlEndpointSelectScrollableCursor
        implements Component, SequentialExecution {

    /**
     * Wrap that enable us to check if there were any results.
     */
    private static class ResultHandlerWrap implements TupleQueryResultHandler {

        private final TupleQueryResultHandler wrap;

        public boolean solutionHandled = false;

        public boolean bindingHandled = false;

        public ResultHandlerWrap(TupleQueryResultHandler wrap) {
            this.wrap = wrap;
        }

        @Override
        public void handleBoolean(boolean value)
                throws QueryResultHandlerException {
            wrap.handleBoolean(value);
        }

        @Override
        public void handleLinks(List<String> linkUrls)
                throws QueryResultHandlerException {
            wrap.handleLinks(linkUrls);
        }

        @Override
        public void startQueryResult(List<String> bindingNames)
                throws TupleQueryResultHandlerException {
            if (!bindingHandled) {
                wrap.startQueryResult(bindingNames);
                bindingHandled = true;
            }
        }

        @Override
        public void endQueryResult() throws TupleQueryResultHandlerException {
            // no-operation
            // We call the super.endQueryResult after all is read
            // from our custom method.
        }

        @Override
        public void handleSolution(BindingSet bindingSet)
                throws TupleQueryResultHandlerException {
            wrap.handleSolution(bindingSet);
            solutionHandled = true;
        }

        public void handleEnd() throws TupleQueryResultHandlerException {
            wrap.endQueryResult();
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(SparqlEndpointSelectScrollableCursor.class);

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public SparqlEndpointSelectScrollableCursorConfiguration configuration;

    @Override
    public void execute() throws LpException {
        final Repository repository = new SPARQLRepository(
                configuration.getEndpoint());
        repository.initialize();
        //
        LOG.info("Used query: {}", prepareQuery(0));
        final File outputFile = outputFiles.createFile(
                configuration.getFileName());
        try (final OutputStream stream = new FileOutputStream(outputFile)) {
            final ResultHandlerWrap writer = createWriter(stream);
            //
            int offset = 0;
            while (true) {
                writer.solutionHandled = false;
                LOG.info("offset: {}", offset);
                executeQuery(repository, writer, offset);
                if (!writer.solutionHandled) {
                    break;
                }
                offset += configuration.getPageSize();
            }
            writer.handleEnd();
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't save data.", ex);
        } catch (Throwable t) {
            throw exceptionFactory.failure("Can't query remote SPARQL.", t);
        } finally {
            try {
                repository.shutDown();
            } catch (RepositoryException ex) {
                LOG.error("Can't close repository.", ex);
            }
        }
    }

    protected static ResultHandlerWrap createWriter(OutputStream stream) {
        final SPARQLResultsCSVWriterFactory writerFactory =
                new SPARQLResultsCSVWriterFactory();
        return new ResultHandlerWrap(writerFactory.getWriter(stream));
    }

    /**
     * @param repository
     * @param handler
     * @param offset
     */
    protected void executeQuery(Repository repository,
            TupleQueryResultHandler handler, int offset) throws LpException {
        try (final RepositoryConnection connection =
                     repository.getConnection()) {
            //
            final TupleQuery query = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, prepareQuery(offset));
            //
            final SimpleDataset dataset = new SimpleDataset();
            for (String iri : configuration.getDefaultGraphs()) {
                if (!iri.isEmpty()) {
                    dataset.addDefaultGraph(
                            SimpleValueFactory.getInstance().createIRI(iri));
                }
            }
            query.setDataset(dataset);
            //
            query.evaluate(handler);
        }
    }

    protected String prepareQuery(int offset) {
        return configuration.getPrefixes() + "\n SELECT " +
                configuration.getOuterSelect() + "\n WHERE { {" +
                configuration.getInnerSelect() +
                "\n} }" +
                "\nLIMIT " + Integer.toString(configuration.getPageSize()) +
                "\nOFFSET " + Integer.toString(offset);
    }

}
