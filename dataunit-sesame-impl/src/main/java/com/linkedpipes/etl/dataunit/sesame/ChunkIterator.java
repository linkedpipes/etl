package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.ChunkedStatements;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Iterator;

public class ChunkIterator implements Iterator<ChunkedStatements.Chunk> {

    private final Iterator<File> directoryIterator;

    private Iterator<File> fileIterator = null;

    private ChunkedStatements.Chunk nextChunk;

    private ChunkedDataUnit dataUnit;

    public ChunkIterator(Iterator<File> directoryIterator,
            ChunkedDataUnit dataUnit) {
        this.directoryIterator = directoryIterator;
        this.dataUnit = dataUnit;
        prepareNext();
    }

    @Override
    public boolean hasNext() {
        return nextChunk != null;
    }

    @Override
    public ChunkedStatements.Chunk next() {
        ChunkedStatements.Chunk chunk = nextChunk;
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
