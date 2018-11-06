package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.AbstractDataUnit;
import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


class DefaultChunkedTriples
        extends AbstractDataUnit
        implements ChunkedTriples, WritableChunkedTriples,
        ManageableDataUnit {

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultChunkedTriples.class);

    private final File writeDirectory;

    private final List<File> dataDirectories = new LinkedList<>();

    private int fileCounter = 0;

    public DefaultChunkedTriples(
            DataUnitConfiguration configuration,
            Collection<String> sources) {
        super(configuration, sources);
        this.writeDirectory = configuration.getWorkingDirectory();
        if (this.writeDirectory != null) {
            this.dataDirectories.add(this.writeDirectory);
            this.writeDirectory.mkdirs();
        }
    }

    @Override
    public void initialize(File directory) throws LpException {
        this.dataDirectories.clear();
        this.dataDirectories.addAll(loadDataDirectories(directory));
    }

    @Override
    public void initialize(
            Map<String, ManageableDataUnit> dataUnits) throws LpException {
        initializeFromSource(dataUnits);
    }

    @Override
    public void save(File directory) throws LpException {
        saveDataDirectories(directory, this.dataDirectories);
        saveDebugDirectories(directory, this.dataDirectories);
    }

    @Override
    public void close() {
        // No operation here.
    }

    @Override
    public void submit(Collection<Statement> statements) throws LpException {
        File outputFile =
                new File(this.writeDirectory, ++this.fileCounter + ".ttl");
        try (OutputStream stream = new FileOutputStream(outputFile);
                Writer writer = new OutputStreamWriter(stream, "UTF-8")) {
            Rio.write(statements, writer, RDFFormat.TURTLE);
        } catch (IOException ex) {
            throw new LpException("Can't save chunk.", ex);
        }
    }

    @Override
    public long size() {
        LOG.debug("Computing size ...");
        int size = 0;
        for (ChunkedTriples.Chunk chunk : this) {
            size++;
        }
        LOG.debug("Computing size ... done");
        return size;
    }

    @Override
    public Iterator<Chunk> iterator() {
        return new ChunkIterator(this.dataDirectories.iterator());
    }

    @Override
    public Collection<File> getSourceDirectories() {
        return Collections.unmodifiableCollection(this.dataDirectories);
    }

    @Override
    protected void merge(ManageableDataUnit dataUnit) throws LpException {
        if (dataUnit instanceof DefaultChunkedTriples) {
            DefaultChunkedTriples source = (DefaultChunkedTriples) dataUnit;
            this.dataDirectories.addAll(source.dataDirectories);
        } else {
            throw new LpException(
                    "Can't merge with source data unit: {} of type {}",
                    getIri(), dataUnit.getClass().getSimpleName());
        }
    }

}
