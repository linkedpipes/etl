package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.JsonUtils;
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
import java.util.*;
import java.util.stream.Collectors;

class DefaultGraphListDataUnit extends BaseRdf4jDataUnit
        implements GraphListDataUnit, WritableGraphListDataUnit {

    /**
     * Base IRI for graphs in this data utni.
     */
    private String baseGraph = null;

    private final List<IRI> graphs = new LinkedList<>();

    public DefaultGraphListDataUnit(String binding, String iri,
            Repository repository,
            Collection<String> sources, String baseGraph) {
        super(binding, iri, repository, sources);
        this.baseGraph = baseGraph;
    }

    @Override
    public IRI createGraph() throws LpException {
        final IRI graph = VF.createIRI(baseGraph + "/" + graphs.size());
        graphs.add(graph);
        return graph;
    }

    @Override
    public Collection<IRI> getReadGraphs() throws LpException {
        return graphs;
    }

    @Override
    public void initialize(File directory) throws LpException {
        // Load metadata.
        JsonUtils.loadCollection(new File(directory, "data.json"),
                String.class).forEach((item) -> graphs.add(VF.createIRI(item)));
        // Load data.
        execute((connection) -> {
            try {
                connection.add(new File(directory, "data/data.trig"),
                        "http://localhost/base/", RDFFormat.TRIG);
            } catch (IOException ex) {
                throw new LpException("Can't load data file");
            }
        });
    }

    @Override
    public void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException {
        // Iterate over sources and add their content.
        for (String iri : sources) {
            if (!dataUnits.containsKey(iri)) {
                throw new LpException("Missing input: {}", iri);
            }
            final ManageableDataUnit dataunit = dataUnits.get(iri);
            if (dataunit instanceof DefaultGraphListDataUnit) {
                merge((DefaultGraphListDataUnit) dataunit);
            } else {
                throw new LpException(
                        "Can't merge with source data unit: {} of type {}",
                        iri, dataunit.getClass().getSimpleName());
            }
        }
    }

    @Override
    public void save(File directory) throws LpException {
        // Save metadata file.
        final List<String> directories = graphs.stream().map(
                (item) -> item.stringValue()).collect(Collectors.toList());
        JsonUtils.save(new File(directory, "data.json"), directories);
        // Save data file.
        final File dataDirectory = new File(directory, "data");
        dataDirectory.mkdirs();
        final File dataFile = new File(dataDirectory, "data.trig");
        execute((connection) -> {
            try (FileOutputStream stream = new FileOutputStream(dataFile)) {
                final RDFWriter writer
                        = Rio.createWriter(RDFFormat.TRIG, stream);
                connection.export(writer, graphs.toArray(new IRI[0]));
            } catch (IOException ex) {
                throw new LpException("Can't write data to file.", ex);
            }
        });
        //
        final List<String> debugDirectories = Arrays.asList(
                directory.toPath().relativize(dataDirectory.toPath())
                        .toString());
        JsonUtils.save(new File(directory, "debug.json"), debugDirectories);
    }

    @Override
    public void close() throws LpException {
        // No operation here.
    }

    protected void merge(DefaultGraphListDataUnit source) throws LpException {
        // TODO Check, that we use same repository
        graphs.addAll(source.getReadGraphs());
    }

}
