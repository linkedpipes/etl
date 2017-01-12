package com.linkedpipes.etl.dataunit.core.rdf;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Iterator;

public class ChunkIterator implements Iterator<ChunkedTriples.Chunk> {

    private final Iterator<File> directoryIterator;

    private Iterator<File> fileIterator = null;

    private ChunkedTriples.Chunk nextChunk;

    private DefaultChunkedTriples dataUnit;

    public ChunkIterator(Iterator<File> directoryIterator,
            DefaultChunkedTriples dataUnit) {
        this.directoryIterator = directoryIterator;
        this.dataUnit = dataUnit;
        prepareNext();
    }

    @Override
    public boolean hasNext() {
        return nextChunk != null;
    }

    @Override
    public ChunkedTriples.Chunk next() {
        ChunkedTriples.Chunk chunk = nextChunk;
        prepareNext();
        return chunk;
    }

    private void prepareNext() {
        if (fileIterator != null && fileIterator.hasNext()) {
            nextChunk = dataUnit.createChunk(fileIterator.next());
        } else if (directoryIterator.hasNext()) {
            fileIterator = FileUtils.iterateFiles(
                    directoryIterator.next(), null, true);
            prepareNext();
        } else {
            nextChunk = null;
        }
    }

}
