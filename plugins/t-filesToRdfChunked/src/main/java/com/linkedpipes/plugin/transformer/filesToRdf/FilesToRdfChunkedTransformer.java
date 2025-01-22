package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkExecution;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkTransformer;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.eclipse.rdf4j.rio.jsonld.JSONLDParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class FilesToRdfChunkedTransformer
        extends ChunkTransformer<FilesContainer, Collection<Statement>> {

    protected static final Logger LOG =
            LoggerFactory.getLogger(FilesToRdfChunkedTransformer.class);

    protected final FilesToRdfConfiguration configuration;

    protected final RDFFormat defaultFormat;

    protected final List<Statement> buffer = new ArrayList<>(100000);

    protected final String blankNodePrefix;

    public FilesToRdfChunkedTransformer(
            ChunkExecution<FilesContainer, Collection<Statement>> owner,
            FilesToRdfConfiguration configuration,
            RDFFormat defaultFormat,
            String blankNodePrefix) {
        super(owner);
        this.configuration = configuration;
        this.defaultFormat = defaultFormat;
        this.blankNodePrefix = blankNodePrefix;
    }

    @Override
    protected Collection<Statement> processChunk(FilesContainer filesContainer)
            throws LpException {
        buffer.clear();
        for (FilesDataUnit.Entry entry : filesContainer.getFiles()) {
            LOG.debug("Loading: {}", entry.getFileName());
            try {
                loadEntry(entry);
            } catch (Throwable ex) {
                if (configuration.isSkipOnFailure()) {
                    LOG.warn("Failed loading {}",
                            entry.getFileName(), ex);
                    continue;
                }
                throw new LpException("Failed loading file: {}",
                        entry.getFileName(), ex);
            }
        }
        return buffer;
    }

    protected void loadEntry(FilesDataUnit.Entry entry) throws LpException {
        RDFFormat format = getFormat(entry.getFileName());
        loadFile(entry.toFile(), format);
        if (configuration.isFileReference()) {
            ValueFactory valueFactory = SimpleValueFactory.getInstance();
            buffer.add(valueFactory.createStatement(
                    valueFactory.createBNode(),
                    valueFactory.createIRI(configuration.getFilePredicate()),
                    valueFactory.createLiteral(entry.getFileName())
            ));
        }
    }

    protected RDFFormat getFormat(String fileName) throws LpException {
        if (defaultFormat != null) {
            return defaultFormat;
        }
        Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
        if (format.isEmpty()) {
            throw new LpException(
                    "Can't determine format for file: {}", fileName);
        }
        return format.get();
    }

    protected void loadFile(File file, RDFFormat format) throws LpException {
        RDFParser parser;
        try {
            parser = createParser(format);
        } catch(RuntimeException ex) {
            throw new LpException("Can't create parser for a file: {}", file, ex);
        }

        if (parser instanceof JSONLDParser jsonLdParser) {
            jsonLdParser.set(JSONLDSettings.SECURE_MODE, false);
        }

        try (InputStream stream = new FileInputStream(file)) {
            parser.parse(stream, "http://localhost/base/");
        } catch (RuntimeException | IOException ex) {
            throw new LpException("Can't load file: {}", file, ex);
        }
    }

    protected RDFParser createParser(RDFFormat format) {
        RDFHandler handler = new AbstractRDFHandler() {
            @Override
            public void handleStatement(Statement st) {
                buffer.add(st);
            }
        };
        if (format == RDFFormat.JSONLD) {
            handler = new BlankNodePrefixUpdater(handler, blankNodePrefix);
        }
        RDFParser parser = Rio.createParser(format);
        parser.setRDFHandler(handler);
        return parser;
    }

}
