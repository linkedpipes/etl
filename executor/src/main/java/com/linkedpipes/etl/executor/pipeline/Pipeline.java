package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.pipeline.model.ConfigurationDescription;
import com.linkedpipes.etl.executor.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.rdf.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import com.linkedpipes.etl.unpacker.ExecutionSource;
import com.linkedpipes.etl.unpacker.TemplateSource;
import com.linkedpipes.etl.unpacker.UnpackerException;
import com.linkedpipes.etl.unpacker.UnpackerFacade;
import com.linkedpipes.etl.unpacker.template.GraphTemplateSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

/**
 * Represent a pipeline that should be executed.
 */
public class Pipeline {

    private static final String DESCRIPTION = "http://plugins.linkedpipes.com/ontology/ConfigurationDescription";

    private PipelineModel model;

    /**
     * Repository used to store pipeline object.
     */
    private Repository repository;

    private Rdf4jSource source;

    /**
     * Unpack a raw (Designer-authored, template-referencing) pipeline
     * definition and its options into a resolved pipeline ready to run.
     *
     * @param definitionFile   Raw pipeline definition; self-contained,
     *                         carrying every referenced template's RDF.
     * @param optionsFile      Unpack options, may be {@code null} if none
     *                         were provided.
     * @param repositoryDirectory Directory for the repository backing the
     *                         resolved pipeline.
     * @param executionSource  Resolves prior-execution data for
     *                         debug/resume/mapped-component unpacking.
     */
    public void load(
            File definitionFile, File optionsFile, File repositoryDirectory, ExecutionSource executionSource)
            throws ExecutorException {
        Statements rawDefinition = readStatements(definitionFile);
        Statements options = optionsFile == null ? Statements.arrayList() : readStatements(optionsFile);
        TemplateSource templateSource = new GraphTemplateSource(rawDefinition.selector());
        UnpackerFacade unpacker = new UnpackerFacade(templateSource, executionSource);
        Collection<Statement> resolved;
        try {
            resolved = unpacker.unpack(rawDefinition, options);
        } catch (UnpackerException ex) {
            throw new ExecutorException("Can't unpack pipeline.", ex);
        }
        loadFromStatements(resolved, repositoryDirectory);
    }

    private Statements readStatements(File file) throws ExecutorException {
        Statements statements = Statements.arrayList();
        try {
            statements.file().addAll(file);
        } catch (IOException ex) {
            throw new ExecutorException("Can't read '{}'.", file, ex);
        }
        return statements;
    }

    private void loadFromStatements(Collection<Statement> statements, File repositoryDirectory)
            throws ExecutorException {
        // Create repository and load pipeline.
        repository = new SailRepository(new NativeStore(repositoryDirectory));
        repository.init();
        try (final RepositoryConnection connection = repository.getConnection()) {
            connection.add(statements);
        } catch (Exception ex) {
            throw new ExecutorException("Can't load definition.", ex);
        }
        source = Rdf4jSource.wrapRepository(repository);
        // Search for a pipeline.
        String iri;
        String graph;
        try {
            List<Map<String, String>> bindings = RdfUtils.sparqlSelect(source, getPipelineQuery());
            if (bindings.size() != 1) {
                throw new ExecutorException("Invalid number of pipelines: {}", bindings.size());
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
        for (PipelineComponent component : model.getComponents()) {
            ConfigurationDescription description = component.getConfigurationDescription();
            if (description == null) {
                continue;
            }
            try {
                String descriptionResource = RdfUtils.sparqlSelectSingle(
                        source,
                        "SELECT ?s WHERE { "
                                + "GRAPH <" + description.getIri() + "> {"
                                + "?s ?p <" + DESCRIPTION + "> "
                                + "} } LIMIT 1",
                        "graph");
                RdfUtils.load(source, descriptionResource, description.getIri(), description);
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

    public Rdf4jSource getSource() {
        return source;
    }

    public PipelineModel getModel() {
        return model;
    }

    /**
     * Save content of the pipeline definition into given file.
     */
    public void save(File path) throws ExecutorException {
        RDFFormat format = Rio.getWriterFormatForFileName(path.getName()).orElseThrow(() -> new ExecutorException(""));
        try (OutputStream stream = new FileOutputStream(path)) {
            RDFWriter writer = Rio.createWriter(format, stream);
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
     */
    public BackendTripleWriter configurationWriter(PipelineComponent component, String graph) {
        // TODO Save component so we know the owner.
        return source.getTripleWriter(graph);
    }

    private static String getPipelineQuery() {
        return "SELECT ?pipeline ?graph WHERE { GRAPH ?graph {\n"
                + " ?pipeline a <" + LP_PIPELINE.PIPELINE + "> \n"
                + "}}";
    }
}
