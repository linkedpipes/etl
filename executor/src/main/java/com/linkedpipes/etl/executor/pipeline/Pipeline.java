package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.pipeline.model.Component;
import com.linkedpipes.etl.executor.pipeline.model.ConfigurationDescription;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Represent a pipeline that should be executed.
 */
public class Pipeline {

    private static final Logger LOG = LoggerFactory.getLogger(Pipeline.class);

    private PipelineModel model;

    /**
     * Repository used to store pipeline object.
     */
    private Repository repository;

    private RdfSource source;

    /**
     * Load pipeline definition from given file.
     *
     * @param file
     * @param repositoryDirectory
     */
    public void load(File file, File repositoryDirectory)
            throws ExecutorException {
        // Check for definition file.
        final RDFFormat rdfFormat = Rio.getParserFormatForFileName(
                file.getName()).orElse(null);
        if (rdfFormat == null) {
            throw new ExecutorException("Invalid definition format.");
        }
        // Create repository and load pipeline.
        repository = new SailRepository(new NativeStore(repositoryDirectory));
        repository.initialize();
        try (final RepositoryConnection connection
                     = repository.getConnection()) {
            connection.add(file, "http://localhost/base", rdfFormat);
        } catch (Exception ex) {
            throw new ExecutorException("Can't load definition.", ex);
        }
        source = Rdf4jSource.wrapRepository(repository);
        // Search for a pipeline.
        final String iri;
        final String graph;
        try {
            final List<Map<String, String>> bindings = RdfUtils.sparqlSelect(
                    source, getPipelineQuery());
            if (bindings.size() != 1) {
                throw new ExecutorException("Invalid number of pipelines: {}",
                        bindings.size());
            }
            iri = bindings.get(0).get("pipeline");
            graph = bindings.get(0).get("graph");
        } catch (RdfUtilsException ex) {
            throw new ExecutorException("Can't query for pipeline object.", ex);
        }
        // Parse data.
        model = new PipelineModel(iri, graph);
        try {
            RdfUtils.load(source, iri, graph, model);
        } catch (RdfUtilsException ex) {
            throw new ExecutorException("Can't load pipeline model.", ex);
        }
        // Load descriptions.
        for (Component component : model.getComponents()) {
            ConfigurationDescription description =
                    component.getConfigurationDescription();
            if (description == null) {
                continue;
            }
            try {
                // TODO Implement more efficient approach
                String descriptionGraph = RdfUtils.sparqlSelectSingle(source,
                        "SELECT ?graph WHERE { GRAPH ?graph {" +
                                "<" + description.getIri() + "> ?p ?o " +
                                "} } LIMIT 1", "graph");

                RdfUtils.load(source, description.getIri(),
                        descriptionGraph, description);
            } catch (RdfUtilsException ex) {
                throw new ExecutorException("Can't load pipeline model.", ex);
            }
        }
        model.afterLoad();
    }

    public String getPipelineIri() {
        return model.getIri();
    }

    public String getPipelineGraph() {
        return model.getGraph();
    }

    public RdfSource getSource() {
        return source;
    }

    public PipelineModel getModel() {
        return model;
    }

    /**
     * Save content of the pipeline definition into given file.
     *
     * @param path
     */
    public void save(File path) throws ExecutorException {
        final RDFFormat format = Rio.getWriterFormatForFileName(
                path.getName()).orElseThrow(() -> new ExecutorException(""));
        try (final OutputStream stream = new FileOutputStream(path)) {
            final RDFWriter writer = Rio.createWriter(format, stream);
            Repositories.consume(repository, (connection) -> {
                connection.export(writer);
            });
        } catch (RuntimeException | IOException ex) {
            throw new ExecutorException("Can't save pipeline.", ex);
        }
    }

    public void closeRepository() {
        if (repository != null) {
            repository.shutDown();
        }
    }

    /**
     * Return writer for configuration of given component to given graph.
     * The given graph is set as a configuration for given component.
     * The writer write statements into the pipeline definition. The
     * writer must be closed for changes to apply.
     *
     * @param component
     * @param graph
     * @return Writer for given configuration.
     */
    public TripleWriter setConfiguration(
            Component component, String graph)
            throws ExecutorException {
        LOG.info("setConfiguration {} {}", component.getIri(), graph);
        // TODO Save reference to the entity.
        return source.getTripleWriter(graph);
    }

    private static String getPipelineQuery() {
        return "SELECT ?pipeline ?graph WHERE { GRAPH ?graph {\n" +
                " ?pipeline a <" + LP_PIPELINE.PIPELINE + "> \n" +
                "}}";
    }

}
