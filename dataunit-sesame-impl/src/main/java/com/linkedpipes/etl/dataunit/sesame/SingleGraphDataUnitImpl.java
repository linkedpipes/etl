package com.linkedpipes.etl.dataunit.sesame;

import java.util.List;
import java.util.Map;
import org.openrdf.model.IRI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException.LocalizedString;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import org.openrdf.IsolationLevels;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store all triples in a single graph.
 *
 * @author Škoda Petr
 */
public class SingleGraphDataUnitImpl extends SesameDataUnitImpl
        implements ManagableSingleGraphDataUnit {

    private final static String QUERY_COPY
            = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    private static final Logger LOG
            = LoggerFactory.getLogger(SingleGraphDataUnitImpl.class);

    /**
     * Current data graph.
     */
    private IRI graph = null;

    public SingleGraphDataUnitImpl(IRI graphIri, Repository repository,
            RdfDataUnitConfiguration configuration) {
        super(repository, configuration);
        this.graph = graphIri;
    }

    protected void merge(SingleGraphDataUnitImpl source)
            throws ManagableDataUnit.DataUnitException {
        try {
            execute((connection) -> {
                final Update update = connection.prepareUpdate(
                        QueryLanguage.SPARQL, QUERY_COPY);
                final SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(source.graph);
                dataset.setDefaultInsertGraph(graph);
                update.setDataset(dataset);
                update.execute();
            });
        } catch (RepositoryActionFailed ex) {
            throw new DataUnitException("Can't merge data.", ex);
        }
    }

    @Override
    public IRI getGraph() {
        return graph;
    }

    @Override
    public void initialize(File directory) throws DataUnitException {
        final File dataFile = new File(directory, "data.ttl");
        final RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        //
        try {
            execute((connection) -> {
                final RDFInserter inserter = new RDFInserter(connection);
                inserter.enforceContext(graph);
                rdfParser.setRDFHandler(inserter);
                LOG.debug("initialize: loading ... {}", dataFile.getPath());
                connection.begin(IsolationLevels.NONE);
                try (final InputStream fileStream
                        = new FileInputStream(dataFile.getPath())) {
                    rdfParser.parse(fileStream, "http://localhost/base");
                } catch (IOException ex) {
                    throw new NonRecoverableException(Arrays.asList(
                            new LocalizedString("Can't read file.", "en")), ex);
                }
                LOG.debug("initialize: commiting ...");
                connection.commit();
            });
        } catch (RepositoryActionFailed ex) {
            throw new DataUnitException("Can't load data file.", ex);
        }
        LOG.debug("initialize: done");
    }

    @Override
    public void initialize(Map<String, ManagableDataUnit> dataUnits)
            throws DataUnitException {
        // Merge content of other data units.
        for (String sourceUri : sources) {
            if (!dataUnits.containsKey(sourceUri)) {
                throw new DataUnitException("Missing input!");
            }
            final ManagableDataUnit dataunit = dataUnits.get(sourceUri);
            if (dataunit instanceof SingleGraphDataUnitImpl) {
                merge((SingleGraphDataUnitImpl) dataunit);
            } else {
                throw new DataUnitException(
                        "Can't merge with source data unit!");
            }
        }
        initialized = true;
    }

    @Override
    public void save(File directory) throws DataUnitException {
        final File dataFile = new File(directory, "data.ttl");
        try {
            execute((connection) -> {
                try (FileOutputStream outputStream
                        = new FileOutputStream(dataFile)) {
                    final RDFWriter writer
                            = Rio.createWriter(RDFFormat.TURTLE, outputStream);
                    connection.export(writer,
                            Arrays.asList(graph).toArray(new IRI[0]));
                } catch (IOException ex) {
                    throw new NonRecoverableException(Arrays.asList(
                            new LocalizedString(
                                    "Can't write data to file.", "en")),
                            ex);
                }
            });
        } catch (SesameDataUnitException ex) {
            throw new DataUnitException("Can't write debug data.", ex);
        }
    }

    @Override
    public List<File> dumpContent(File directory) throws DataUnitException {
        // TODO We could use the save output here as a debug.
        save(directory);
        return Collections.EMPTY_LIST;
    }

    @Override
    public void close() throws DataUnitException {
        // No operation here.
    }

    @Override
    public List<Map<String, String>> executeSelect(String query)
            throws QueryException {
        try {
            return execute((connection) -> {
                List<Map<String, String>> output = new LinkedList<>();
                final TupleQuery tupleQuery = connection.prepareTupleQuery(
                        QueryLanguage.SPARQL, query);
                final SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(graph);
                tupleQuery.setDataset(dataset);
                final TupleQueryResult result = tupleQuery.evaluate();
                while (result.hasNext()) {
                    final BindingSet binding = result.next();
                    final Map<String, String> record = new HashMap<>();
                    for (Binding item : binding) {
                        record.put(item.getName(),
                                item.getValue().stringValue());
                    }
                    output.add(record);
                }
                return output;
            });
        } catch (RepositoryActionFailed ex) {
            throw new QueryException("Can't query data.", ex);
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

}
