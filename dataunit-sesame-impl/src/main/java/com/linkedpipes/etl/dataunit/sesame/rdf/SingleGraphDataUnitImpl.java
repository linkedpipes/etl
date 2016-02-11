package com.linkedpipes.etl.dataunit.sesame.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.RecoverableException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.openrdf.model.IRI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.Repository;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

/**
 * Store all triples in a single graph.
 *
 * @author Škoda Petr
 */
class SingleGraphDataUnitImpl implements ManagableSingleGraphDataUnit {

    private final static String QUERY_COPY = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    /**
     * Determine if this data unit was initialized or not.
     */
    protected boolean initialized = false;

    private final GraphListDataUnitImpl dataUnit;

    /**
     * Current data graph.
     */
    private IRI graph = null;

    /**
     * List of source data unit URIs.
     */
    protected final Collection<String> sources;

    public SingleGraphDataUnitImpl(IRI metadataGraphUri, Repository repository,
            RdfDataUnitConfiguration configuration) {
        dataUnit = new GraphListDataUnitImpl(metadataGraphUri, repository, configuration);
        this.sources = configuration.getSourceDataUnitUris();
    }

    protected void merge(SingleGraphDataUnitImpl source) throws ManagableDataUnit.DataUnitException {
        try {
            dataUnit.execute((connection) -> {
                try {
                    final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, QUERY_COPY);
                    final DatasetImpl dataset = new DatasetImpl();
                    dataset.addDefaultGraph(source.graph);
                    dataset.setDefaultInsertGraph(graph);
                    update.setDataset(dataset);
                    update.execute();
                } catch (RepositoryException | MalformedQueryException | UpdateExecutionException ex) {
                    throw new RecoverableException(ex);
                }
            });
        } catch (RepositoryActionFailed ex) {
            throw new DataUnitException("Can't metge data.", ex);
        }
    }

    @Override
    public void initialize(Map<String, ManagableDataUnit> dataUnits) throws DataUnitException {
        try {
            graph = dataUnit.createGraph();
        } catch (SesameDataUnitException ex) {
            throw new DataUnitException("Can't initialize source graph.", ex);
        }
        // Merge content of other data units.
        for (String sourceUri : sources) {
            if (!dataUnits.containsKey(sourceUri)) {
                throw new DataUnitException("Missing input!");
            }
            final ManagableDataUnit dataunit = dataUnits.get(sourceUri);
            if (dataunit instanceof SingleGraphDataUnitImpl) {
                merge((SingleGraphDataUnitImpl) dataunit);
            } else {
                throw new DataUnitException("Can't merge with source data unit!");
            }
        }
        initialized = true;
    }

    @Override
    public void dumpContent() throws DataUnitException {
        dataUnit.dumpContent();
    }

    @Override
    public void close() throws DataUnitException {
        dataUnit.close();
    }

    @Override
    public IRI getGraph() {
        return graph;
    }

    @Override
    public List<Map<String, String>> executeSelect(String query) throws QueryException {
        return dataUnit.executeSelect(query);
    }

    @Override
    public void execute(RepositoryProcedure action) throws RepositoryActionFailed {
        dataUnit.execute(action);
    }

    @Override
    public <T> T execute(RepositoryFunction<T> action) throws RepositoryActionFailed {
        return dataUnit.execute(action);
    }

    @Override
    public void execute(Procedure action) throws RepositoryActionFailed {
        dataUnit.execute(action);
    }

    @Override
    public Repository getRepository() {
        return dataUnit.getRepository();
    }

    @Override
    public String getBinding() {
        return dataUnit.getBinding();
    }

    @Override
    public String getResourceUri() {
        return dataUnit.getResourceUri();
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

}
