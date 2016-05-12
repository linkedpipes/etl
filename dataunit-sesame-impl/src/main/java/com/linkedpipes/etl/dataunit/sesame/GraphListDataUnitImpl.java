package com.linkedpipes.etl.dataunit.sesame;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException.LocalizedString;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.etl.executor.api.v1.exception.RecoverableException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.openrdf.IsolationLevels;
import org.openrdf.OpenRDFException;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.AbstractRDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilize one graph (so called "metadata graph") to store references to
 * other graphs, where the data are located.
 *
 * @author Škoda Petr
 */
public final class GraphListDataUnitImpl extends SesameDataUnitImpl
        implements ManagableGraphListDataUnit {

    public static final String HAS_GRAPH
            = "http://linkedpipes.com/ontology/dataUnit/sesame/1.0/graph";

    private static final Logger LOG
            = LoggerFactory.getLogger(GraphListDataUnitImpl.class);

    /**
     * IRI of metadata graph.
     */
    private final IRI metadataGraphIri;

    private final ValueFactory factory = SimpleValueFactory.getInstance();

    /**
     * Count number of stored graphs.
     */
    private int graphCounter = 0;

    public GraphListDataUnitImpl(IRI metadataGraphIri, Repository repository,
            RdfDataUnitConfiguration configuration) {
        super(repository, configuration);
        this.metadataGraphIri = metadataGraphIri;
    }

    protected void merge(GraphListDataUnitImpl source)
            throws ManagableDataUnit.DataUnitException {
        try {
            for (IRI graph : source.getGraphs()) {
                addGraph(graph);
            }
        } catch (SesameDataUnitException ex) {
            throw new ManagableDataUnit.DataUnitException(
                    "Can't add graph during merge.", ex);
        }
    }

    protected void addGraph(IRI graphUri) throws SesameDataUnitException {
        execute((connection) -> {
            try {
                connection.add(factory.createStatement(metadataGraphIri,
                        factory.createIRI(HAS_GRAPH), graphUri),
                        metadataGraphIri);
            } catch (RepositoryException ex) {
                throw new RecoverableException(Arrays.asList(
                        new LocalizedString("Can't add graph record.", "en")),
                        ex);
            }
        });
    }

    @Override
    public IRI createGraph() throws SesameDataUnitException {
        final IRI graphUri = createGraphIRI();
        addGraph(graphUri);
        return graphUri;
    }

    @Override
    public Collection<IRI> getGraphs() throws SesameDataUnitException {
        return execute((connection) -> {
            try {
                final TupleQueryResult result = connection.prepareTupleQuery(
                        QueryLanguage.SPARQL,
                        getQueryGraphs(metadataGraphIri.stringValue()))
                        .evaluate();
                final List<IRI> graphList = new LinkedList<>();
                while (result.hasNext()) {
                    final BindingSet binding = result.next();
                    graphList.add((IRI) binding.getBinding("graph").getValue());
                }
                return graphList;
            } catch (RepositoryException | MalformedQueryException |
                    QueryEvaluationException ex) {
                throw new RecoverableException(Arrays.asList(
                        new LocalizedString("Can't list graphs.", "en")), ex);
            }
        });
    }

    @Override
    public void initialize(File directory) throws DataUnitException {
        final File dataFile = new File(directory, "data.trig");
        final RDFParser rdfParser = Rio.createParser(RDFFormat.TRIG);
        //
        final Map<Resource, IRI> graphs = new HashMap<>();
        try {
            execute((connection) -> {
                // We need to add data to our graphs.
                rdfParser.setRDFHandler(new AbstractRDFInserter(
                        connection.getValueFactory()) {

                    @Override
                    protected void addNamespace(String prefix, String name)
                            throws OpenRDFException {
                        if (connection.getNamespace(prefix) == null) {
                            connection.setNamespace(prefix, name);
                        }
                    }

                    @Override
                    protected void addStatement(Resource subj, IRI pred,
                            Value obj, Resource ctxt) throws OpenRDFException {
                        if (!graphs.containsKey(ctxt)) {
                            final IRI graphUri = createGraphIRI();
                            graphs.put(ctxt, graphUri);
                        }
                        connection.add(subj, pred, obj, graphs.get(ctxt));
                    }

                });
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
        // Add graphs.
        try {
            for (IRI graphIRI : graphs.values()) {
                addGraph(graphIRI);
            }
        } catch (SesameDataUnitException ex) {
            throw new DataUnitException("Can't add loaded graphs.", ex);
        }
        LOG.debug("initialize: done");
    }

    @Override
    public void initialize(Map<String, ManagableDataUnit> dataUnits)
            throws DataUnitException {
        for (String sourceUri : sources) {
            if (!dataUnits.containsKey(sourceUri)) {
                throw new DataUnitException("Missing input!");
            }
            final ManagableDataUnit dataunit = dataUnits.get(sourceUri);
            if (dataunit instanceof GraphListDataUnitImpl) {
                merge((GraphListDataUnitImpl) dataunit);
            } else {
                throw new DataUnitException(
                        "Can't merge with source data unit!");
            }
        }
        initialized = true;
    }

    @Override
    public void save(File directory) throws DataUnitException {
        final File dataFile = new File(directory, "data.trig");
        try {
            final Collection<IRI> graphs = getGraphs();
            execute((connection) -> {
                try (FileOutputStream outputStream
                        = new FileOutputStream(dataFile)) {
                    final RDFWriter writer
                            = Rio.createWriter(RDFFormat.TRIG, outputStream);
                    connection.export(writer, graphs.toArray(new IRI[0]));
                } catch (IOException ex) {
                    throw new NonRecoverableException(
                            Arrays.asList(new LocalizedString(
                                    "Can't write data to file.", "en")), ex);
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
                for (IRI graph : getGraphs()) {
                    dataset.addDefaultGraph(graph);
                }
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

    public long size() throws SesameDataUnitException {
        return getGraphs().size();
    }

    private IRI createGraphIRI() {
        return factory.createIRI(metadataGraphIri.stringValue()
                + "/dataGraph/" + Integer.toString(++graphCounter));
    }

    private static String getQueryGraphs(String iri) {
        return "SELECT ?graph WHERE { <" + iri + "> "
                + "<" + HAS_GRAPH + "> "
                + "?graph . }";
    }

}
