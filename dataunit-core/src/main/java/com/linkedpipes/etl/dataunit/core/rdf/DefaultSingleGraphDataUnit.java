package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.RuntimeConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class DefaultSingleGraphDataUnit extends BaseRdf4jDataUnit
        implements SingleGraphDataUnit, WritableSingleGraphDataUnit,
        RuntimeConfiguration {

    private final static String QUERY_COPY
            = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    private IRI graph = null;

    public DefaultSingleGraphDataUnit(String binding, String iri,
            Repository repository, Collection<String> sources, String graph) {
        super(binding, iri, repository, sources);
        this.graph = VF.createIRI(graph);
    }

    @Override
    public IRI getWriteGraph() {
        return graph;
    }

    @Override
    public TripleWriter getWriter() {
        return new Rdf4jTripleWriter(graph, this);
    }

    @Override
    public IRI getReadGraph() {
        return graph;
    }

    @Override
    public RdfSource asRdfSource() {
        return new Rdf4jRdfSource(this, graph);
    }

    @Override
    public void initialize(File directory) throws LpException {
        final List<File> directories = loadDataDirectories(directory);
        if (directories.size() != 1) {
            throw new LpException("Invalid number of directories {} in {}",
                    directories.size(), directory);
        }
        final File dataDirectory = directories.get(0);
        loadContentFromTurtle(dataDirectory);
    }

    @Override
    public void save(File directory) throws LpException {
        final File dataDirectory = new File(directory, "data");
        saveContentAsTurtle(dataDirectory);
        saveDataDirectories(directory, Arrays.asList(dataDirectory));
        saveDebugDirectories(directory, Arrays.asList(dataDirectory));
    }

    @Override
    public void close() throws LpException {
        // No operation here.
    }

    @Override
    public void write(TripleWriter writer) throws LpException {
        execute((connection) -> {
            final RepositoryResult<Statement> statements =
                    connection.getStatements(null, null, null, graph);
            while (statements.hasNext()) {
                final Statement statement = statements.next();
                writeStatement(statement, writer);
            }
        });
    }

    @Override
    protected void merge(ManageableDataUnit dataunit) throws LpException {
        if (dataunit instanceof DefaultSingleGraphDataUnit) {
            merge((DefaultSingleGraphDataUnit) dataunit);
        } else {
            throw new LpException(
                    "Can't merge with source data unit: {} of type {}",
                    getIri(), dataunit.getClass().getSimpleName());
        }
    }

    private void merge(DefaultSingleGraphDataUnit source) throws LpException {
        // TODO Check that we use the same repository.
        try {
            execute((connection) -> {
                final Update update = connection.prepareUpdate(
                        QueryLanguage.SPARQL, QUERY_COPY);
                final SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(source.getReadGraph());
                dataset.setDefaultInsertGraph(graph);
                update.setDataset(dataset);
                update.execute();
            });
        } catch (LpException ex) {
            throw new LpException("Can't merge with: {}",
                    source.getIri(), ex);
        }
    }

    private void writeStatement(Statement statement,
            TripleWriter writer) throws LpException {
        final String subject = statement.getSubject().stringValue();
        final String predicate = statement.getPredicate().stringValue();
        final Value object = statement.getObject();
        if (object instanceof IRI) {
            writer.iri(subject, predicate, object.stringValue());
        } else if (object instanceof Literal) {
            final Literal literal = (Literal) object;
            if (literal.getLanguage().isPresent()) {
                writer.string(subject, predicate, literal.stringValue(),
                        literal.getLanguage().get());
            } else {
                writer.typed(subject, predicate, literal.stringValue(),
                        literal.getDatatype().stringValue());
            }
        } else {
            throw new LpException("Invalid statement: {} {} {}",
                    statement.getSubject(),
                    statement.getPredicate(),
                    statement.getObject());
        }
    }

    private void saveContentAsTurtle(File dataDirectory) throws LpException {
        dataDirectory.mkdirs();
        final File file = new File(dataDirectory, "data.ttl");
        execute((connection) -> {
            try (FileOutputStream stream = new FileOutputStream(file)) {
                final RDFWriter writer
                        = Rio.createWriter(RDFFormat.TURTLE, stream);
                connection.export(writer, graph);
            } catch (IOException ex) {
                throw new LpException("Can't write data to file.", ex);
            }
        });
    }

    private void loadContentFromTurtle(File dataDirectory) throws LpException {
        execute((connection) -> {
            try {
                connection.add(new File(dataDirectory, "data.ttl"),
                        "http://localhost/base/", RDFFormat.TURTLE,
                        graph);
            } catch (IOException ex) {
                throw new LpException("Can't load data file for {} from {}",
                        getIri(), dataDirectory);
            }
        });
    }

}
