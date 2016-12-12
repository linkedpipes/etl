package com.linkedpipes.etl.dataunit.sesame;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.ChunkedStatements;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableChunkedStatements;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

class ChunkedDataUnit implements ChunkedStatements, WritableChunkedStatements,
        ManageableDataUnit {

    class ChunkImpl implements ChunkedStatements.Chunk {

        private final File file;

        public ChunkImpl(File file) {
            this.file = file;
        }

        @Override
        public Collection<Statement> toStatements() throws LpException {
            LOG.info("Loading: {} ... ", file);
            final List<Statement> statements = new LinkedList<>();
            try (InputStream stream = new FileInputStream(file);
                 Reader reader = new InputStreamReader(stream, "UTF-8")) {
                RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
                parser.setRDFHandler(new AbstractRDFHandler() {
                    @Override
                    public void handleStatement(Statement st)
                            throws RDFHandlerException {
                        statements.add(st);
                    }
                });
                parser.parse(reader, "http://localhost/base/");
            } catch (IOException ex) {
                throw ExceptionFactory.failure("Can't load chunk.", ex);
            }
            LOG.info("Loading: {} ... done", file);
            return statements;
        }
    }

    private static final Logger LOG
            = LoggerFactory.getLogger(ChunkedDataUnit.class);

    private final String id;

    private final String resourceUri;

    private boolean initialized = false;

    private File writeDirectory;

    private final List<File> readDirectories = new LinkedList<>();

    private final Collection<String> sources;

    private int fileCounter = 0;

    public ChunkedDataUnit(RdfDataUnitConfiguration configuration) {
        this.id = configuration.getBinding();
        this.resourceUri = configuration.getResourceIri();
        this.sources = configuration.getSourceDataUnitIris();
        String workingDirectory = configuration.getWorkingDirectory();
        if (workingDirectory != null) {
            this.writeDirectory =
                    new File(java.net.URI.create(workingDirectory));
            this.writeDirectory.mkdirs();
            this.readDirectories.add(this.writeDirectory);
        }
    }

    @Override
    public void submit(Collection<Statement> statements) throws LpException {
        final File outputFile =
                new File(writeDirectory, ++fileCounter + ".ttl");
        try (OutputStream stream = new FileOutputStream(outputFile);
             Writer writer = new OutputStreamWriter(stream, "UTF-8")) {
            Rio.write(statements, writer, RDFFormat.TURTLE);
        } catch (IOException ex) {
            throw ExceptionFactory.failure("Can't save chunk.", ex);
        }
    }

    @Override
    public long size() {
        int size = 0;
        for (Chunk chunk : this) {
            size++;
        }
        return size;
    }

    @Override
    public Iterator<Chunk> iterator() {
        return new ChunkIterator(readDirectories.iterator(), this);
    }

    @Override
    public String getBinding() {
        return id;
    }

    @Override
    public String getResourceIri() {
        return resourceUri;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void initialize(File directory) throws LpException {
        // This is read-only mode.
        final ObjectMapper mapper = new ObjectMapper();
        final File inputFile = new File(directory, "data.json");
        final JavaType type = mapper.getTypeFactory().constructCollectionType(
                List.class, String.class);
        final List<String> relativePaths;
        try {
            relativePaths = mapper.readValue(inputFile, type);
        } catch (IOException ex) {
            throw ExceptionFactory.initializationFailed(
                    "Can't load directory list.", ex);
        }
        for (String path : relativePaths) {
            readDirectories.add(new File(directory, path));
        }
    }

    @Override
    public void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException {
        if (writeDirectory == null) {
            throw ExceptionFactory.initializationFailed(
                    "Root directory is not set!");
        }
        for (String sourceUri : sources) {
            if (!dataUnits.containsKey(sourceUri)) {
                throw ExceptionFactory.initializationFailed(
                        "Missing input: {}", sourceUri);
            }
            final ManageableDataUnit dataunit = dataUnits.get(sourceUri);
            if (dataunit instanceof ChunkedDataUnit) {
                readDirectories.addAll(
                        ((ChunkedDataUnit) dataunit).readDirectories);
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
        final ObjectMapper mapper = new ObjectMapper();
        final File outputFile = new File(directory, "data.json");
        final List<String> relativeDirectories = new ArrayList<>(
                readDirectories.size());
        for (File file : readDirectories) {
            relativeDirectories.add(directory.toPath().relativize(
                    file.toPath()).toString());
        }
        try {
            mapper.writeValue(outputFile, relativeDirectories);
        } catch (IOException ex) {
            throw ExceptionFactory.failure(
                    "Can't save directory list.", ex);
        }
        // Return list of all used data directories with the data.
        return readDirectories;
    }

    @Override
    public void close() throws LpException {
        // No-operation here.
    }

    ChunkedStatements.Chunk createChunk(File file) {
        return new ChunkImpl(file);
    }

}
