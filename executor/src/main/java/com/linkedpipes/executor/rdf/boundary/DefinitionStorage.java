package com.linkedpipes.executor.rdf.boundary;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.executor.rdf.controller.ConnectionAction;
import com.linkedpipes.executor.rdf.util.SesameUtils;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 *
 * @author Škoda Petr
 */
public class DefinitionStorage implements DataUnit, SparqlSelect {

    private static final Logger LOG = LoggerFactory.getLogger(DefinitionStorage.class);

    public static final String ID = "system.definition";

    private final Repository repository;

    private String definitionGraphUri = null;

    public DefinitionStorage(File workingDir) throws RepositoryException {
        repository = new SailRepository(new NativeStore(workingDir));
        repository.initialize();
    }

    public void load(File file, RDFFormat format) throws RdfOperationFailed {
        try {
            ConnectionAction.call(repository, (connection) -> {
                if (!format.supportsContexts()) {
                    // File without graphs, we assign a default graph.
                    // TODO We can assign graph based on execution uri here.
                    final IRI graphUri = SimpleValueFactory.getInstance().createIRI("http://localhost/temp/definition");
                    SesameUtils.load(connection, file, format, graphUri);
                } else {
                    SesameUtils.load(connection, file, format);
                }
            });
        } catch (ConnectionAction.CallFailed ex) {
            throw new RdfOperationFailed("Can't load definition DataUnit.", ex);
        }
    }

    public void store(File file, RDFFormat format) {
        try {
            ConnectionAction.call(repository, (connection) -> {
                SesameUtils.store(connection, file, format);
            });
        } catch (ConnectionAction.CallFailed ex) {
            LOG.info("Can't store definition DataUnit.", ex);
        }
    }

    public void close() {
        try {
            repository.shutDown();
        } catch (RepositoryException ex) {
            LOG.error("Can't close repository.", ex);
        }
    }

    public WritableRdfJava asWritableRdfJava() {
        return new WritableRdfJava(repository, getDefinitionGraphUri());
    }

    public void setDefinitionGraphUri(String graphUri) {
        definitionGraphUri = graphUri;
    }

    public String getDefinitionGraphUri() {
        if (definitionGraphUri == null) {
            throw new RuntimeException("'definitionGraphUri' has not been set before usage!");
        }
        return definitionGraphUri;
    }

    @Override
    public String getBinding() {
        return ID;
    }

    @Override
    public String getResourceUri() {
        return getDefinitionGraphUri();
    }

    @Override
    public boolean isInitialized() {
        return repository != null;
    }

    @Override
    public List<Map<String, String>> executeSelect(String query) throws SparqlSelect.QueryException {
        try {
            return ConnectionAction.call(repository, (connection) -> {
                return SesameUtils.executeSelect(connection, query);
            });
        } catch (ConnectionAction.CallFailed ex) {
            throw new SparqlSelect.QueryException("Can't query definition DataUnit.", ex);
        }
    }

}
