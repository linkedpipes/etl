package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.executor.rdf.PojoLoader;
import org.apache.commons.io.FileUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.repository.util.Repositories;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provide access to the RDF pipeline definition.
 */
public class PipelineDefinition implements SparqlSelect {

    public static class InitializationFailed extends Exception {

        public InitializationFailed(String message) {
            super(message);
        }

        public InitializationFailed(Throwable cause) {
            super(cause);
        }

        public InitializationFailed(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private static final Logger LOG
            = LoggerFactory.getLogger(PipelineDefinition.class);

    private final Repository repository;

    /**
     * Store IRI of the definition graph.
     */
    private String definitionGraph;

    private PipelineModel pipelineModel;

    private File repositoryDirectory;

    public PipelineDefinition(File workingDir) throws RepositoryException {
        repository = new SailRepository(new NativeStore(workingDir));
        repository.initialize();
        this.repositoryDirectory = workingDir;
    }

    public Repository getRepository() {
        return repository;
    }

    public String getDefinitionGraph() {
        return definitionGraph;
    }

    public PipelineModel getPipelineModel() {
        return pipelineModel;
    }

    /**
     * Load the pipeline definition.
     *
     * @param resourceManager
     */
    public void initialize(ResourceManager resourceManager)
            throws InitializationFailed {
        try (RepositoryConnection connection = repository.getConnection()) {
            load(connection, resourceManager.getDefinitionFile());
        } catch (RepositoryException ex) {
            throw new InitializationFailed(ex);
        }
        // Get definition graph.
        final String pipelineResource;
        try {
            final String query = "SELECT ?s ?g WHERE {\n"
                    + "  GRAPH ?g { ?s a <"
                    + LINKEDPIPES.PIPELINE + "> . }\n}";
            final BindingSet result = Repositories.tupleQuery(repository,
                    query, (x) -> QueryResults.singleResult(x));
            if (result == null) {
                throw new InitializationFailed(
                        "Can't find graph pipeline definition resource.");
            }
            definitionGraph = result.getBinding("g").getValue().stringValue();
            pipelineResource = result.getBinding("s").getValue().stringValue();
        } catch (RepositoryException ex) {
            throw new InitializationFailed(ex);
        }
        // Load pipeline model.
        pipelineModel = new PipelineModel(pipelineResource);
        try {
            PojoLoader.load(repository, pipelineResource,
                    definitionGraph, pipelineModel);
        } catch (LpException ex) {
            throw new InitializationFailed(
                    "Can't load pipeline definition.", ex);
        }
        // Resolve requirements.
        try {
            RequirementProcessor.handle(this, resourceManager);
        } catch (RequirementProcessor.ProcessingFailed ex) {
            throw new InitializationFailed("Can't resolve requirements.", ex);
        }
        // Store.
        try (RepositoryConnection connection = repository.getConnection()) {
            store(connection, resourceManager.getPipelineFile());
        } catch (Exception ex) {
            throw new InitializationFailed("Can't save definition.", ex);
        }
    }

    public void close() {
        try {
            LOG.info("Saving repository ... ");
            repository.shutDown();
            LOG.info("Saving repository ... done");
            // Delete the directory.
            FileUtils.deleteQuietly(repositoryDirectory);
            while (repositoryDirectory.exists()) {
                // It may take some time until the repository is
                // ready to be deleted.
                try {
                    LOG.debug("Waiting on released of repository.");
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {

                }
                FileUtils.deleteQuietly(repositoryDirectory);
            }
        } catch (RepositoryException ex) {
            LOG.error("Can't close the repository.", ex);
        }
    }

    @Override
    public List<Map<String, String>> executeSelect(String query) {
        try (RepositoryConnection connection = repository.getConnection()) {
            return executeSelect(connection, query);
        }
    }

    private static void load(RepositoryConnection connection, File file)
            throws InitializationFailed {
        final RDFFormat format = Rio.getParserFormatForFileName(file.getName())
                .orElse(null);
        if (format == null) {
            throw new InitializationFailed("Can't determine file type.");
        }
        final RDFParser rdfParser = Rio.createParser(format);
        final RDFInserter inserter = new RDFInserter(connection);
        rdfParser.setRDFHandler(inserter);
        // Load file.
        try (InputStream input = new FileInputStream(file)) {
            rdfParser.parse(input, "http://localhost/");
        } catch (IOException | OpenRDFException ex) {
            throw new InitializationFailed(ex);
        }
    }

    private static void store(RepositoryConnection connection, File file)
            throws IOException {
        try (OutputStream stream = new FileOutputStream(file)) {
            final RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, stream);
            connection.export(writer);
        }
    }

    private static List<Map<String, String>> executeSelect(
            RepositoryConnection connection, String query)
            throws RepositoryException {
        final List<Map<String, String>> output = new LinkedList<>();
        // Evaluate query.
        final TupleQueryResult result = connection.prepareTupleQuery(
                QueryLanguage.SPARQL, query).evaluate();
        // Store result, convert everything into strings.
        while (result.hasNext()) {
            final BindingSet binding = result.next();
            final Map<String, String> bindingAsMap
                    = new HashMap<>(binding.size());
            binding.getBindingNames().forEach((name) -> {
                bindingAsMap.put(name, binding.getValue(name).stringValue());
            });
            output.add(bindingAsMap);
        }
        return output;
    }

}
