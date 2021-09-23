package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkExecution;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkTransformer;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class FilesToRdfChunked
        extends ChunkExecution<FilesContainer, Collection<Statement>>
        implements Component {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.Configuration
    public FilesToRdfConfiguration configuration;

    protected Integer executorCounter = 0;

    @Override
    protected Iterator<FilesContainer> chunks() {
        Iterator<FilesDataUnit.Entry> source = inputFiles.iterator();
        Integer filesPerChunk = configuration.getFilesPerChunk();
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return source.hasNext();
            }

            @Override
            public FilesContainer next() {
                List<FilesDataUnit.Entry> files =
                        new ArrayList<>(filesPerChunk);
                for (int counter = 0; counter < filesPerChunk ; ++counter) {
                    if (!source.hasNext()) {
                        break;
                    }
                    files.add(source.next());
                }
                if (files.size() == 0) {
                    return null;
                }
                return new FilesContainer(files);
            }

        };
    }

    @Override
    protected int getThreadCount() {
        return configuration.getThreadCount();
    }

    @Override
    protected long getChunkCount() {
        double fileCount = inputFiles.size();
        double filesPerChunk = Math.max(1, configuration.getFilesPerChunk());
        return Math.round(Math.ceil(fileCount / filesPerChunk));
    }

    @Override
    protected ChunkTransformer<FilesContainer, Collection<Statement>>
    createExecutor() {
        RDFFormat defaultFormat = getDefaultFormat();
        String blankNodePrefix = Integer.toString(executorCounter++);
        return new FilesToRdfChunkedTransformer(
                this, configuration, defaultFormat, blankNodePrefix);
    }

    private RDFFormat getDefaultFormat() {
        String mimeType = configuration.getMimeType();
        if (mimeType == null || mimeType.isEmpty()) {
            return null;
        }
        return Rio.getParserFormatForMIMEType(
                configuration.getMimeType()).get();
    }

    @Override
    protected boolean shouldSkipFailures() {
        return configuration.isSkipOnFailure();
    }

    @Override
    protected void submitInternal(Collection<Statement> statements)
            throws LpException {
        outputRdf.submit(statements);
        statements.clear();
    }

}
