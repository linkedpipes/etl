package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

class DefaultGraphListDataUnit extends BaseRdf4jDataUnit
        implements GraphListDataUnit, WritableGraphListDataUnit {

    private String graphPrefixIri = null;

    private final List<IRI> graphs = new LinkedList<>();

    public DefaultGraphListDataUnit(String binding, String iri,
            Repository repository,
            Collection<String> sources, String baseGraph) {
        super(binding, iri, repository, sources);
        this.graphPrefixIri = baseGraph;
    }

    @Override
    public IRI createGraph() throws LpException {
        final IRI graph = VF.createIRI(graphPrefixIri + "/" + graphs.size());
        graphs.add(graph);
        return graph;
    }

    @Override
    public Collection<IRI> getReadGraphs() throws LpException {
        return graphs;
    }

    @Override
    public void initialize(File directory) throws LpException {
        final List<File> directories = loadDataDirectories(directory);
        if (directories.size() != 1) {
            throw new LpException("Invalid number of directories {} in {}",
                    directories.size(), directory);
        }
        final File dataDirectory = directories.get(0);
        loadContentFromTrig(dataDirectory);
    }

    @Override
    public void save(File directory) throws LpException {
        final File dataDirectory = new File(directory, "data");
        saveContentAsTrig(dataDirectory);
        saveDataDirectories(directory, Arrays.asList(dataDirectory));
        saveDebugDirectories(directory, Arrays.asList(dataDirectory));
    }

    @Override
    public void close() throws LpException {
        // No operation here.
    }

    @Override
    protected void merge(ManageableDataUnit dataunit) throws LpException {
        if (dataunit instanceof DefaultGraphListDataUnit) {
            final DefaultGraphListDataUnit source =
                    (DefaultGraphListDataUnit) dataunit;
            graphs.addAll(source.getReadGraphs());
        } else {
            throw new LpException(
                    "Can't merge with source data unit: {} of type {}",
                    getIri(), dataunit.getClass().getSimpleName());
        }
    }

    private void saveContentAsTrig(File dataDirectory) throws LpException {
        dataDirectory.mkdirs();
        final File file = new File(dataDirectory, "data.trig");
        execute((connection) -> {
            try (FileOutputStream stream = new FileOutputStream(file)) {
                final RDFWriter writer
                        = Rio.createWriter(RDFFormat.TRIG, stream);
                connection.export(writer, graphs.toArray(new IRI[0]));
            } catch (IOException ex) {
                throw new LpException("Can't write data to file.", ex);
            }
        });
    }

    private void loadContentFromTrig(File dataDirectory) throws LpException {
        execute((connection) -> {
            try {
                connection.add(new File(dataDirectory, "data.trig"),
                        "http://localhost/base/", RDFFormat.TRIG);
            } catch (IOException ex) {
                throw new LpException("Can't load data file");
            }
        });
    }

}
