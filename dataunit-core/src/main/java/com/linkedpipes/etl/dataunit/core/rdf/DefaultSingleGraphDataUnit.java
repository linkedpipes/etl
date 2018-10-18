package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
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
import java.util.Map;

class DefaultSingleGraphDataUnit extends BaseRdf4jDataUnit
        implements SingleGraphDataUnit, WritableSingleGraphDataUnit,
        RuntimeConfiguration {

    private final static String COPY_QUERY
            = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    private IRI graph;

    public DefaultSingleGraphDataUnit(
            DataUnitConfiguration configuration,
            RepositoryManager manager,
            Collection<String> sources) {
        super(configuration, sources, manager);
    }

    @Override
    public IRI getWriteGraph() {
        return this.graph;
    }

    @Override
    public TripleWriter getWriter() {
        return new Rdf4jTripleWriter(this.graph, this);
    }

    @Override
    public IRI getReadGraph() {
        return this.graph;
    }

    @Override
    public RdfSource asRdfSource() {
        return new Rdf4jRdfSource(this, this.graph);
    }

    @Override
    public void initialize(File directory) throws LpException {
        super.initialize(directory);
        List<File> directories = loadDataDirectories(directory);
        if (directories.size() != 1) {
            throw new LpException("Invalid number of directories {} in {}",
                    directories.size(), directory);
        }
        File dataDirectory = directories.get(0);
        this.graph = VF.createIRI(this.configuration.getResource());
        loadContentFromTurtle(dataDirectory);
    }

    @Override
    public void initialize(
            Map<String, ManageableDataUnit> dataUnits) throws LpException {
        super.initialize(dataUnits);
        if (this.sources.size() == 1) {
            String sourceIri = this.sources.iterator().next();
            ManageableDataUnit source = dataUnits.get(sourceIri);
            consumeInput(source);
        } else {
            this.graph = VF.createIRI(this.configuration.getResource());
            initializeFromSource(dataUnits);
        }
    }

    private void consumeInput(ManageableDataUnit dataUnit) throws LpException {
        if (!(dataUnit instanceof DefaultSingleGraphDataUnit)) {
            throw new LpException(
                    "Can't merge with source data unit: {} of type {}",
                    getIri(), dataUnit.getClass().getSimpleName());
        }
        DefaultSingleGraphDataUnit source =
                (DefaultSingleGraphDataUnit) dataUnit;
        this.graph = source.graph;
    }

    @Override
    public void save(File directory) throws LpException {
        File dataDirectory = new File(directory, "data");
        saveContentAsTurtle(dataDirectory);
        saveDataDirectories(directory, Arrays.asList(dataDirectory));
        saveDebugDirectories(directory, Arrays.asList(dataDirectory));
    }

    @Override
    public void close() {
        this.repositoryManager.closeRepository(this.getRepository());
    }

    @Override
    public void write(TripleWriter writer) throws LpException {
        execute((connection) -> {
            RepositoryResult<Statement> statements =
                    connection.getStatements(null, null, null, graph);
            while (statements.hasNext()) {
                writeStatement(statements.next(), writer);
            }
        });
    }

    @Override
    protected void merge(ManageableDataUnit dataUnit) throws LpException {
        if (dataUnit instanceof DefaultSingleGraphDataUnit) {
            merge((DefaultSingleGraphDataUnit) dataUnit);
        } else {
            throw new LpException(
                    "Can't merge with source data unit: {} of type {}",
                    getIri(), dataUnit.getClass().getSimpleName());
        }
    }

    private void merge(DefaultSingleGraphDataUnit source) throws LpException {
        if (this.getRepository() != source.getRepository()) {
            throw new LpException("Source have different repository.");
        }
        try {
            execute((connection) -> {
                Update update = connection.prepareUpdate(
                        QueryLanguage.SPARQL, COPY_QUERY);
                SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(source.getReadGraph());
                dataset.setDefaultInsertGraph(graph);
                update.setDataset(dataset);
                update.execute();
            });
        } catch (LpException ex) {
            throw new LpException(
                    "Can't merge with: {}", source.getIri(), ex);
        }
    }

    private void writeStatement(
            Statement statement, TripleWriter writer) throws LpException {
        String subject = statement.getSubject().stringValue();
        String predicate = statement.getPredicate().stringValue();
        Value object = statement.getObject();
        if (object instanceof IRI) {
            writer.iri(subject, predicate, object.stringValue());
        } else if (object instanceof Literal) {
            Literal literal = (Literal) object;
            if (literal.getLanguage().isPresent()) {
                writer.string(
                        subject,
                        predicate,
                        literal.stringValue(),
                        literal.getLanguage().get());
            } else {
                writer.typed(
                        subject,
                        predicate,
                        literal.stringValue(),
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
        File file = new File(dataDirectory, "data.ttl");
        execute((connection) -> {
            try (FileOutputStream stream = new FileOutputStream(file)) {
                RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, stream);
                connection.export(writer, this.graph);
            } catch (IOException ex) {
                throw new LpException("Can't write data to file.", ex);
            }
        });
    }

    private void loadContentFromTurtle(File dataDirectory) throws LpException {
        execute((connection) -> {
            try {
                connection.add(
                        new File(dataDirectory, "data.ttl"),
                        "http://localhost/base/",
                        RDFFormat.TURTLE, this.graph);
            } catch (IOException ex) {
                throw new LpException(
                        "Can't load data file for {} from {}",
                        getIri(), dataDirectory);
            }
        });
    }

}
