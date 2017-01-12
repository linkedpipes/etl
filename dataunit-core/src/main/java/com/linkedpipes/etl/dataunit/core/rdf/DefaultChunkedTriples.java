package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.JsonUtils;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

class DefaultChunkedTriples implements ChunkedTriples, WritableChunkedTriples,
        ManageableDataUnit {

    class Chunk implements ChunkedTriples.Chunk {

        private final File file;

        public Chunk(File file) {
            this.file = file;
        }

        @Override
        public Collection<Statement> toCollection() throws LpException {
            final List<Statement> statements = new LinkedList<>();
            try (InputStream stream = new FileInputStream(file);
                 Reader reader = new InputStreamReader(stream, "UTF-8")) {
                final RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
                parser.setRDFHandler(new AbstractRDFHandler() {
                    @Override
                    public void handleStatement(Statement st)
                            throws RDFHandlerException {
                        statements.add(st);
                    }
                });
                parser.parse(reader, "http://localhost/base/");
            } catch (IOException ex) {
                throw new LpException("Can't load chunk.", ex);
            }
            return statements;
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultChunkedTriples.class);

    private final String binding;

    private final String iri;

    private final File writeDirectory;

    private final List<File> readDirectories = new LinkedList<>();

    private final Collection<String> sources;

    private int fileCounter = 0;

    public DefaultChunkedTriples(String binding, String iri, File directory,
            Collection<String> sources) {
        this.binding = binding;
        this.iri = iri;
        this.writeDirectory = directory;
        this.sources = sources;
        if (writeDirectory != null) {
            this.readDirectories.add(writeDirectory);
            writeDirectory.mkdirs();
        }
    }

    @Override
    public String getBinding() {
        return binding;
    }

    @Override
    public String getIri() {
        return iri;
    }

    @Override
    public void initialize(File directory) throws LpException {
        final File file = new File(directory, "data.json");
        JsonUtils.loadCollection(file, String.class).stream().forEach(
                (entry) -> readDirectories.add(new File(directory, entry))
        );
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
            if (dataunit instanceof DefaultChunkedTriples) {
                merge((DefaultChunkedTriples) dataunit);
            } else {
                throw new LpException(
                        "Can't merge with source data unit: {} of type {}",
                        iri, dataunit.getClass().getSimpleName());
            }
        }
    }

    @Override
    public List<File> save(File directory) throws LpException {
        final Path dirPath = directory.toPath();
        final List<String> directories = readDirectories.stream().map(
                (file) -> dirPath.relativize(file.toPath()).toString()
        ).collect(Collectors.toList());
        JsonUtils.save(new File(directory, "data.json"), directories);
        return readDirectories;
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
        return null;
    }

    protected void merge(DefaultChunkedTriples source) throws LpException {
        this.readDirectories.addAll(source.readDirectories);
    }

    /**
     * @param file
     * @return Chunk representation for given file.
     */
    Chunk createChunk(File file) {
        return new Chunk(file);
    }

}
