package com.linkedpipes.etl.rdf.utils.rdf4j;

import com.linkedpipes.etl.rdf.utils.model.ClosableRdfSource;
import org.eclipse.rdf4j.repository.Repository;

public class ClosableRdf4jSource extends Rdf4jSource
        implements ClosableRdfSource {

    public ClosableRdf4jSource(Repository repository) {
        super(repository);
    }

    @Override
    public void close() {
        repository.shutDown();
    }

}
