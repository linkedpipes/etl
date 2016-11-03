package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.IsolationLevels;
import org.openrdf.OpenRDFException;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.*;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.AbstractRDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Utilize one graph (so called "metadata graph") to store references to
 * other graphs, where the data are located.
 */
public final class GraphListDataUnitImpl extends SesameDataUnitImpl
        implements ManageableGraphListDataUnit {

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

    protected void merge(GraphListDataUnitImpl source) throws LpException {
        try {
            for (IRI graph : source.getGraphs()) {
                addGraph(graph);
            }
        } catch (LpException ex) {
            throw ExceptionFactory.failure("Can't merge with: {}",
                    source.getResourceIri(), ex);
        }
    }

    protected void addGraph(IRI graphUri) throws LpException {
        execute((connection) -> {
            try {
                connection.add(factory.createStatement(metadataGraphIri,
                        factory.createIRI(HAS_GRAPH), graphUri),
                        metadataGraphIri);
            } catch (RepositoryException ex) {
                throw ExceptionFactory.failure("Can't add graph record.", ex);
            }
        });
    }

    @Override
    public IRI createGraph() throws LpException {
        final IRI graphUri = createGraphIRI();
        addGraph(graphUri);
        return graphUri;
    }

    @Override
    public Collection<IRI> getGraphs() throws LpException {
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
                throw ExceptionFactory.failure("Can't list graphs.", ex);
            }
        });
    }

    @Override
    public void initialize(File directory) throws LpException {
        final File dataFile = new File(directory, "data.trig");
        final RDFParser rdfParser = Rio.createParser(RDFFormat.TRIG);
        //
        final Map<Resource, IRI> graphs = new HashMap<>();
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
                throw ExceptionFactory.failure("Can't read file.", ex);
            }
            LOG.debug("initialize: committing ...");
            connection.commit();
        });
        // Add graphs.
        for (IRI graphIRI : graphs.values()) {
            addGraph(graphIRI);
        }
        LOG.debug("initialize: done");
    }

    @Override
    public void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException {
        for (String sourceUri : sources) {
            if (!dataUnits.containsKey(sourceUri)) {
                throw ExceptionFactory.initializationFailed("Missing input!");
            }
            final ManageableDataUnit dataunit = dataUnits.get(sourceUri);
            if (dataunit instanceof GraphListDataUnitImpl) {
                merge((GraphListDataUnitImpl) dataunit);
            } else {
                throw ExceptionFactory.initializationFailed(
                        "Can't merge with source data unit: {} of {}",
                        sourceUri, dataunit.getClass().getSimpleName());
            }
        }
        initialized = true;
    }

    @Override
    public List<File> save(File directory) throws LpException {
        final File dataFile = new File(directory, "data.trig");
        final Collection<IRI> graphs = getGraphs();
        execute((connection) -> {
            try (FileOutputStream outputStream
                         = new FileOutputStream(dataFile)) {
                final RDFWriter writer
                        = Rio.createWriter(RDFFormat.TRIG, outputStream);
                connection.export(writer, graphs.toArray(new IRI[0]));
            } catch (IOException ex) {
                throw ExceptionFactory.initializationFailed(
                        "Can't write data to file.", ex);
            }
        });
        return Arrays.asList(directory);
    }

    @Override
    public void close() throws LpException {
        // No operation here.
    }

    @Override
    public List<Map<String, String>> executeSelect(String query)
            throws RdfException {
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
        } catch (LpException ex) {
            throw ExceptionFactory.failure("Can't query data.", ex);
        }
    }

    public long size() throws LpException {
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
