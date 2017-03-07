package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.rdf.AnnotationDescriptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SparqlEndpointList
        implements Component, SequentialExecution {

    private static final ValueFactory VALUE_FACTORY =
            SimpleValueFactory.getInstance();

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        List<SparqlEndpointListConfiguration> configurations;
        try {
            configurations = loadConfigurations();
        } catch (Exception ex) {
            throw exceptionFactory.failure("Can't load configurations.", ex);
        }
        progressReport.start(configurations.size());
        for (SparqlEndpointListConfiguration configuration : configurations) {
            try {
                queryRepository(configuration);
            } catch (Exception ex) {
                throw exceptionFactory.failure("Can't query repository.", ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private List<SparqlEndpointListConfiguration> loadConfigurations()
            throws RdfUtilsException {
        Rdf4jSource source = Rdf4jSource.createWrap(
                configurationRdf.getRepository());
        List<SparqlEndpointListConfiguration> result =
                RdfUtils.loadTypedByReflection(source,
                        configurationRdf.getReadGraph().stringValue(),
                        SparqlEndpointListConfiguration.class,
                        new AnnotationDescriptionFactory());
        source.shutdown();
        return result;
    }

    private void queryRepository(
            SparqlEndpointListConfiguration configuration) {
        SPARQLRepository repository = createRepository(
                configuration.getEndpoint(), configuration.getTransferMimeType()
        );
        repository.initialize();
        executeQueryAndSaveResults(repository,
                configuration.getQuery(), configuration.getDefaultGraphs());
        repository.shutDown();
    }

    private static SPARQLRepository createRepository(String endpoint,
            String mimeType) {
        SPARQLRepository repository = new SPARQLRepository(endpoint);
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (mimeType != null) {
            headers.put("Accept", mimeType);
        }
        repository.setAdditionalHttpHeaders(headers);
        return repository;
    }

    private void executeQueryAndSaveResults(Repository repository, String query,
            List<String> defaultGraph) {
        try (RepositoryConnection connection = repository.getConnection()) {
            GraphQuery preparedQuery = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL, query);
            // Construct dataset.
            final SimpleDataset dataset = new SimpleDataset();
            for (String iri : defaultGraph) {
                dataset.addDefaultGraph(VALUE_FACTORY.createIRI(iri));
            }
            preparedQuery.setDataset(dataset);
            try (GraphQueryResult result = preparedQuery.evaluate()) {
                saveResults(result);
            }
        }
    }

    private void saveResults(GraphQueryResult result) {
        try (RepositoryConnection connection =
                     outputRdf.getRepository().getConnection()) {
            connection.begin();
            connection.add(result, outputRdf.getWriteGraph());
            connection.commit();
        }
    }

}
