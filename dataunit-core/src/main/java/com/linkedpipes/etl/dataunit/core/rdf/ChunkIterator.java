package com.linkedpipes.etl.dataunit.core.rdf;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Iterator;

public class ChunkIterator implements Iterator<ChunkedTriples.Chunk> {

    private final Iterator<File> directoryIterator;

    private Iterator<File> fileIterator = null;

    private ChunkedTriples.Chunk nextChunk;

    public ChunkIterator(Iterator<File> directoryIterator) {
        this.directoryIterator = directoryIterator;
        prepareNext();
    }

    @Override
    public boolean hasNext() {
        return this.nextChunk != null;
    }

    @Override
    public ChunkedTriples.Chunk next() {
        ChunkedTriples.Chunk chunk = this.nextChunk;
        prepareNext();
        return chunk;
    }

    private void prepareNext() {
        if (this.fileIterator != null && this.fileIterator.hasNext()) {
            this.nextChunk = new DefaultChunk(this.fileIterator.next());
        } else if (this.directoryIterator.hasNext()) {
            this.fileIterator = FileUtils.iterateFiles(
                    this.directoryIterator.next(), null, true);
            prepareNext();
        } else {
            this.nextChunk = null;
        }
    }

}
