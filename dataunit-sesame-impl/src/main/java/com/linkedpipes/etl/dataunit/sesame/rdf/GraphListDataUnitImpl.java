package com.linkedpipes.etl.dataunit.sesame.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.etl.executor.api.v1.exception.RecoverableException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.Repository;

import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import org.openrdf.model.IRI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 * Utilize one graph (so called "metadata graph") to store references to other graphs, where the data are located.
 *
 * @author Škoda Petr
 */
final class GraphListDataUnitImpl extends SesameDataUnitImpl implements ManagableGraphListDataUnit {

    /**
     * %s - root metadata resource URI
     */
    private final static String QUERY_GRAPHS = ""
            + "SELECT ?graph WHERE {\n"
            + " <%s> <http://cunifiedviews.opendata.cz/ontology/dataUnit/sesame/graphList/graph> ?graph .\n"
            + "}";

    private final IRI metadataGraphUri;

    private final ValueFactory factory = ValueFactoryImpl.getInstance();

    private int graphCounter = 0;

    public GraphListDataUnitImpl(IRI metadataGraphUri, Repository repository,
            RdfDataUnitConfiguration configuration) {
        super(repository, configuration);
        this.metadataGraphUri = metadataGraphUri;
    }

    protected void merge(GraphListDataUnitImpl source) throws ManagableDataUnit.DataUnitException {
        // Just add records for all graphs.
        try {
            for (IRI graph : source.getGraphs()) {
                addGraph(graph);
            }
        } catch (SesameDataUnitException ex) {
            throw new ManagableDataUnit.DataUnitException("Can't add graph.", ex);
        }
    }

    protected void addGraph(IRI graphUri) throws SesameDataUnitException {
        try {
            execute((connection) -> {
                try {
                    connection.add(factory.createStatement(metadataGraphUri,
                            factory.createURI("http://cunifiedviews.opendata.cz/ontology/dataUnit/sesame/graphList/graph"),
                            graphUri), metadataGraphUri);
                } catch (RepositoryException ex) {
                    throw new RecoverableException(ex);
                }
            });
        } catch (RepositoryActionFailed ex) {
            throw new SesameDataUnitException("Can't add record for new graph.", ex);
        }
    }

    @Override
    public IRI createGraph() throws SesameDataUnitException {
        final IRI graphUri = factory.createIRI(metadataGraphUri.stringValue() + "/dataGraph/"
                + Integer.toString(++graphCounter));
        addGraph(graphUri);
        return graphUri;
    }

    @Override
    public Collection<IRI> getGraphs() throws SesameDataUnitException {
        try {
            return execute((connection) -> {
                try {
                    final TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                            String.format(QUERY_GRAPHS, metadataGraphUri)).evaluate();
                    final List<IRI> graphList = new LinkedList<>();
                    while (result.hasNext()) {
                        final BindingSet binding = result.next();
                        graphList.add((IRI) binding.getBinding("graph").getValue());
                    }
                    return graphList;
                } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                    throw new RecoverableException(ex);
                }
            });
        } catch (RepositoryActionFailed ex) {
            throw new SesameDataUnitException("Can't list graphs.", ex);
        }
    }

    @Override
    public void initialize(Map<String, ManagableDataUnit> dataUnits) throws DataUnitException {

        // TODO: We may add some root subject here!
        // Merge content of other data units.
        for (String sourceUri : sources) {
            if (!dataUnits.containsKey(sourceUri)) {
                throw new DataUnitException("Missing input!");
            }
            final ManagableDataUnit dataunit = dataUnits.get(sourceUri);
            if (dataunit instanceof GraphListDataUnitImpl) {
                merge((GraphListDataUnitImpl) dataunit);
            } else {
                throw new DataUnitException("Can't merge with source data unit!");
            }
        }
        initialized = true;
    }

    @Override
    public void dumpContent() throws DataUnitException {
        if (this.debugDirectory == null) {
            return;
        }

        final File outputDirectory = new File(this.debugDirectory, "dump");
        outputDirectory.mkdirs();
        final File outputFile = new File(outputDirectory, "data.trig");
        final Collection<IRI> graphs;
        try {
            graphs = getGraphs();
            execute((connection) -> {
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    final RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, outputStream);
                    connection.export(writer, graphs.toArray(new IRI[0]));
                } catch (IOException ex) {
                    throw new NonRecoverableException("Can't write data to file.", ex);
                }
            });
        } catch (SesameDataUnitException ex) {
            throw new DataUnitException("Can't write debug data.", ex);
        }
        // Write information into a directory.
        final File infoFile = new File(this.debugDirectory, "info.dat");
        try (FileWriter fileWriter = new FileWriter(infoFile)) {
            fileWriter.append(outputDirectory.getPath());
        } catch (IOException ex) {
            throw new DataUnitException("Can't write debug data.", ex);
        }
    }

    @Override
    public void close() throws DataUnitException {
        // No operation here.
    }

    @Override
    public List<Map<String, String>> executeSelect(String query) throws QueryException {
        try {

            return execute((connection) -> {
                try {
                    List<Map<String, String>> output = new LinkedList<>();
                    final TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                    final DatasetImpl dataset = new DatasetImpl();
                    for (IRI graph : getGraphs()) {
                        dataset.addDefaultGraph(graph);
                    }
                    tupleQuery.setDataset(dataset);
                    final TupleQueryResult result = tupleQuery.evaluate();
                    while (result.hasNext()) {
                        final BindingSet binding = result.next();
                        final Map<String, String> record = new HashMap<>();
                        for (Binding item : binding) {
                            record.put(item.getName(), item.getValue().stringValue());
                        }
                        output.add(record);
                    }
                    return output;
                } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                    throw new RecoverableException(ex);
                }
            });
        } catch (RepositoryActionFailed ex) {
            throw new QueryException("Can't query data.", ex);
        }
    }

}
