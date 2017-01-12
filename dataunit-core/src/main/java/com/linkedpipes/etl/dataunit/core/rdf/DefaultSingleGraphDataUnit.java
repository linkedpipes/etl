package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
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
        implements SingleGraphDataUnit, WritableSingleGraphDataUnit {

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
    public IRI getReadGraph() {
        return graph;
    }

    @Override
    public void initialize(File directory) throws LpException {
        execute((connection) -> {
            try {
                connection.add(new File(directory, "data.ttl"),
                        "http://localhost/",
                        RDFFormat.TURTLE,
                        graph);
            } catch (Exception ex) {
                throw new LpException("Can't read file.", ex);
            }
        });
    }

    @Override
    public void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException {
        for (String source : sources) {
            if (!dataUnits.containsKey(source)) {
                throw new LpException("Missing input: {}", source);
            }
            final ManageableDataUnit dataunit = dataUnits.get(source);
            if (dataunit instanceof DefaultSingleGraphDataUnit) {
                merge((DefaultSingleGraphDataUnit) dataunit);
            } else {
                throw new LpException(
                        "Can't merge with source data unit: {} of {}",
                        source, dataunit.getClass().getSimpleName());
            }
        }
    }

    @Override
    public List<File> save(File directory) throws LpException {
        final File dataFile = new File(directory, "data.ttl");
        execute((connection) -> {
            try (FileOutputStream stream = new FileOutputStream(dataFile)) {
                final RDFWriter writer =
                        Rio.createWriter(RDFFormat.TURTLE, stream);
                connection.export(writer,
                        Arrays.asList(graph).toArray(new IRI[0]));
            } catch (IOException ex) {
                throw new LpException("Can't write data to file.", ex);
            }
        });
        return Arrays.asList(directory);
    }

    @Override
    public void close() throws LpException {
        // No operation here.
    }

    protected void merge(DefaultSingleGraphDataUnit source) throws LpException {
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

}
