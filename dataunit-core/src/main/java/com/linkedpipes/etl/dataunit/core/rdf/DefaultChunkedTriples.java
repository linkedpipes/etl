package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.AbstractDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

class DefaultChunkedTriples
        extends AbstractDataUnit
        implements ChunkedTriples, WritableChunkedTriples,
        ManageableDataUnit {

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultChunkedTriples.class);

    private final File writeDirectory;

    private final List<File> dataDirectories = new LinkedList<>();

    private int fileCounter = 0;

    public DefaultChunkedTriples(String binding, String iri, File directory,
            Collection<String> sources) {
        super(binding, iri, sources);
        this.writeDirectory = directory;
        if (writeDirectory != null) {
            this.dataDirectories.add(writeDirectory);
            writeDirectory.mkdirs();
        }
    }

    @Override
    public void initialize(File directory) throws LpException {
        dataDirectories.clear();
        dataDirectories.addAll(loadDataDirectories(directory));
    }

    @Override
    public void save(File directory) throws LpException {
        saveDataDirectories(directory, dataDirectories);
        saveDebugDirectories(directory, dataDirectories);
    }

    @Override
    public void close() throws LpException {
        // No operation here.
    }

    @Override
    public void submit(Collection<Statement> statements) throws LpException {
        final File outputFile =
                new File(writeDirectory, ++fileCounter + ".ttl");
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
    public Iterator<ChunkedTriples.Chunk> iterator() {
        return new ChunkIterator(dataDirectories.iterator(), this);
    }

    @Override
    public Collection<File> getSourceDirectories() {
        return Collections.unmodifiableCollection(dataDirectories);
    }

    @Override
    protected void merge(ManageableDataUnit dataunit) throws LpException {
        if (dataunit instanceof DefaultChunkedTriples) {
            final DefaultChunkedTriples source =
                    (DefaultChunkedTriples) dataunit;
            dataDirectories.addAll(source.dataDirectories);
        } else {
            throw new LpException(
                    "Can't merge with source data unit: {} of type {}",
                    getIri(), dataunit.getClass().getSimpleName());
        }
    }


}
