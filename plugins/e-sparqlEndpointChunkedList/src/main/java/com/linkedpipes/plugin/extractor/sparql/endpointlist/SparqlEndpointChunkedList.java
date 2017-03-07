package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.rdf.AnnotationDescriptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.plugin.extractor.sparql.endpoint.ValuesSource;
import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Takes CSV files on input. The CSV file rows are used as IRIs and mapped
 * to the given SPARQL as the ${VALUES} placeholder.
 *
 * Example query:
 * CONSTRUCT { ?obec ?p ?o } WHERE { ?obec ?p ?o ${VALUES} }
 * where the input CSV file contains column "obec".
 */
public final class SparqlEndpointChunkedList implements Component,
        SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlEndpointChunkedList.class);

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        List<SparqlEndpointChunkedListConfiguration> configurations;
        try {
            configurations = loadConfigurations();
        } catch (Exception ex) {
            throw exceptionFactory.failure("Can't load configurations.", ex);
        }
        progressReport.start(configurations.size());
        for (SparqlEndpointChunkedListConfiguration configuration :
                configurations) {
            try {
                execute(configuration);
            } catch (Exception ex) {
                throw exceptionFactory.failure("Can't query repository.", ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private List<SparqlEndpointChunkedListConfiguration> loadConfigurations()
            throws RdfUtilsException {
        Rdf4jSource source = Rdf4jSource.createWrap(
                configurationRdf.getRepository());
        List<SparqlEndpointChunkedListConfiguration> result =
                RdfUtils.loadTypedByReflection(source,
                        configurationRdf.getReadGraph().stringValue(),
                        SparqlEndpointChunkedListConfiguration.class,
                        new AnnotationDescriptionFactory());
        source.shutdown();
        return result;
    }

    private void execute(SparqlEndpointChunkedListConfiguration configuration)
            throws LpException {
        final Repository repository = createRepository(
                configuration.getEndpoint(), configuration.getTransferMimeType()
        );
        try {
            repository.initialize();
        } catch (OpenRDFException ex) {
            throw exceptionFactory.failure("Can't connect to endpoint.", ex);
        }
        final List<Statement> buffer = new ArrayList<>(50000);
        try {
            for (FilesDataUnit.Entry entry : inputFiles) {
                final ValuesSource valuesSource = new ValuesSource(
                        entry.toFile(), exceptionFactory,
                        configuration.getChunkSize());
                valuesSource.readSource((valuesClause) -> {
                    buffer.clear();
                    executeQuery(repository, valuesClause, buffer,
                            configuration.getQuery(),
                            configuration.getDefaultGraphs());
                    outputRdf.submit(buffer);
                });
            }
        } finally {
            try {
                repository.shutDown();
            } catch (RepositoryException ex) {
                LOG.error("Can't close repository.", ex);
            }
        }
    }

    private Repository createRepository(String endpoint, String mimeType) {
        final SPARQLRepository repository = new SPARQLRepository(endpoint);
        // Customize repository.
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (mimeType != null) {
            headers.put("Accept", mimeType);
        }
        repository.setAdditionalHttpHeaders(headers);
        return repository;
    }

    private void executeQuery(Repository repository, String valueClause,
            List<Statement> buffer, String query, List<String> defaultGraphs)
            throws LpException {
        final String queryWithValues = query.replace("${VALUES}", valueClause);
        LOG.debug("query:\n{}", queryWithValues);
        try (final RepositoryConnection connection =
                     repository.getConnection()) {
            final GraphQuery preparedQuery = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL, queryWithValues);
            preparedQuery.setDataset(createDataset(defaultGraphs));
            preparedQuery.evaluate(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st)
                        throws RDFHandlerException {
                    buffer.add(st);
                }
            });
        }
    }

    private SimpleDataset createDataset(List<String> defaultGraphs) {
        final SimpleDataset dataset = new SimpleDataset();
        for (String iri : defaultGraphs) {
            dataset.addDefaultGraph(
                    SimpleValueFactory.getInstance().createIRI(iri));
        }
        return dataset;
    }

}
