package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.eclipse.rdf4j.model.IRI;
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
import java.util.Map;
import java.util.stream.Collectors;

class DefaultGraphListDataUnit extends BaseRdf4jDataUnit
        implements GraphListDataUnit, WritableGraphListDataUnit {

    private String graphPrefix;

    private final List<IRI> graphs = new LinkedList<>();

    public DefaultGraphListDataUnit(
            DataUnitConfiguration configuration,
            RepositoryManager manager,
            Collection<String> sources) {
        super(configuration, sources, manager);
        this.graphPrefix = configuration.getResource();
    }

    @Override
    public IRI createGraph() {
        IRI graph = VF.createIRI(this.graphPrefix + "/" + this.graphs.size());
        this.graphs.add(graph);
        return graph;
    }

    @Override
    public Collection<IRI> getReadGraphs() {
        return this.graphs;
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
        loadContent(dataDirectory);
    }

    @Override
    public void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException {
        super.initialize(dataUnits);
        initializeFromSource(dataUnits);
    }

    @Override
    public void save(File directory) throws LpException {
        File dataDirectory = new File(directory, "data");
        saveContent(dataDirectory);
        saveDataDirectories(directory, Arrays.asList(dataDirectory));
        saveDebugDirectories(directory, Arrays.asList(dataDirectory));
    }

    @Override
    public void close() {
        this.repositoryManager.closeRepository(this.getRepository());
    }

    @Override
    protected void merge(ManageableDataUnit dataUnit) throws LpException {
        if (dataUnit instanceof DefaultGraphListDataUnit) {
            DefaultGraphListDataUnit source =
                    (DefaultGraphListDataUnit) dataUnit;
            this.graphs.addAll(source.getReadGraphs());
        } else {
            throw new LpException(
                    "Can't merge with source data unit: {} of type {}",
                    getIri(), dataUnit.getClass().getSimpleName());
        }
    }

    private void saveContent(File dataDirectory) throws LpException {
        dataDirectory.mkdirs();
        File dataFile = new File(dataDirectory, "data.trig");
        execute((connection) -> {
            try (FileOutputStream stream = new FileOutputStream(dataFile)) {
                RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, stream);
                connection.export(writer, this.graphs.toArray(new IRI[0]));
            } catch (IOException ex) {
                throw new LpException("Can't write data to file.", ex);
            }
        });
        File graphFile = new File(dataDirectory, "graph.json");
        List<String> graphAsStr = this.graphs.stream()
                .map(iri -> iri.stringValue())
                .collect(Collectors.toList());
        saveCollectionAsJson(graphFile, graphAsStr);
    }

    private void loadContent(File dataDirectory) throws LpException {
        File dataFile = new File(dataDirectory, "data.trig");
        execute((connection) -> {
            try {
                connection.add(
                        dataFile, "http://localhost/base/", RDFFormat.TRIG);
            } catch (IOException ex) {
                throw new LpException("Can't load data file");
            }
        });
        File graphFile = new File(dataDirectory, "graph.json");
        this.graphs.addAll(loadCollectionFromJson(graphFile, String.class)
                .stream()
                .map(str -> VF.createIRI(str))
                .collect(Collectors.toList()));
    }

}
