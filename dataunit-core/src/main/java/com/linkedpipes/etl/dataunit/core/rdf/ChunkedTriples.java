package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.Statement;

import java.io.File;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represent a collection of chunked statements. The collections
 * are read only.
 *
 * The statements must represent triples, ie no information about
 * graphs (context) must not be included.
 */
public interface ChunkedTriples extends Iterable<ChunkedTriples.Chunk> {

    interface Chunk {

        /**
         * Load and return content of the chunk.
         *
         * @return
         */
        Collection<Statement> toCollection() throws LpException;

    }

    /**
     * @return
     */
    default Stream<Chunk> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    long size();

    /**
     * TODO Move to special interface
     *
     * @return Directories with content of the chunked data unit.
     */
    Collection<File> getSourceDirectories();

}
