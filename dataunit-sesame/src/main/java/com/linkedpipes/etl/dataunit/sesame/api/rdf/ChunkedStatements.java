package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.Statement;

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
public interface ChunkedStatements extends Iterable<ChunkedStatements.Chunk> {

    public interface Chunk {

        /**
         * Load and return content of the chunk.
         *
         * @return
         */
        Collection<Statement> toStatements() throws LpException;

    }

    default Stream<Chunk> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    long size();

}
