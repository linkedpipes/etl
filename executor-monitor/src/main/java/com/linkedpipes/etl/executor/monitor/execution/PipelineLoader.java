package com.linkedpipes.etl.executor.monitor.execution;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openrdf.OpenRDFException;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
class PipelineLoader {

    private static final Logger LOG
            = LoggerFactory.getLogger(PipelineLoader.class);

    private PipelineLoader() {
    }

    /**
     * Load content of a pipeline definition and store it to given execution.
     *
     * Can use both pipeline.jsonld file as well as file in the definition
     * directory as fallback.
     *
     * @param execution
     * @throws IOException
     * @throws ExecutionFacade.OperationFailed
     */
    public static void loadPipeline(Execution execution)
            throws IOException, ExecutionFacade.OperationFailed {
        //
        File pipelineFile
                = new File(execution.getDirectory(), "pipeline.jsonld");
        if (!pipelineFile.exists()) {
            pipelineFile = null;
            // Look in the definition directory.
            File directory = new File(execution.getDirectory(), "definition");
            if (!directory.exists()) {
                throw new ExecutionFacade.OperationFailed(
                        "Missing directory for execution: "
                        + execution.getIri());
            }
            //
            for (File file : directory.listFiles()) {
                if (file.isFile() && file.getName().startsWith("definition")) {
                    pipelineFile = file;
                    break;
                }
            }
            //
            if (pipelineFile == null) {
                throw new ExecutionFacade.OperationFailed(
                        "Missing pipeline file for execution: "
                        + execution.getIri());
            }
        }
        //
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();

        final IRI graph = createGraph(valueFactory, execution.getIri());
        final List<Statement> output = new ArrayList<>(16);
        // Load pipeline.
        final List<Statement> pipelineStatements = loadFile(pipelineFile);
        // Find pipeline subject.
        Resource pipelineResource = null;
        for (Statement statement : pipelineStatements) {
            if (RDF.TYPE.equals(statement.getPredicate())) {
                if (statement.getObject().stringValue().equals(
                        "http://linkedpipes.com/ontology/Pipeline")) {
                    pipelineResource = statement.getSubject();
                    break;
                }
            }
        }
        if (pipelineResource == null) {
            LOG.error("Missing pipeline, execution: {}", execution);
            execution.setPipelineStatements(Collections.EMPTY_LIST);
        }
        // Create output.
        output.add(valueFactory.createStatement(
                pipelineResource, RDF.TYPE,
                valueFactory.createIRI(
                        "http://linkedpipes.com/ontology/Pipeline"),
                graph));

        output.add(valueFactory.createStatement(
                valueFactory.createIRI(execution.getIri()),
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/ontology/pipeline"),
                pipelineResource,
                graph));

        for (Statement statement : pipelineStatements) {
            if (SKOS.PREF_LABEL.equals(statement.getPredicate())) {
                if (statement.getSubject().equals(pipelineResource)) {
                    output.add(valueFactory.createStatement(
                            statement.getSubject(),
                            statement.getPredicate(),
                            statement.getObject(),
                            graph));
                }
            }
        }
        execution.setPipelineStatements(output);
    }

    /**
     * Load statements from given file.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static List<Statement> loadFile(File file) throws IOException,
            ExecutionFacade.OperationFailed {
        try (InputStream input = new FileInputStream(file)) {
            return loadStream(input);
        }
    }

    /**
     * Load statements from given stream.
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private static List<Statement> loadStream(InputStream stream)
            throws IOException, ExecutionFacade.OperationFailed {
        final RDFParser reader = Rio.createParser(RDFFormat.JSONLD,
                SimpleValueFactory.getInstance());
        final List<Statement> statements = new ArrayList<>(64);
        //
        reader.setRDFHandler(new AbstractRDFHandler() {

            @Override
            public void handleStatement(Statement statement) {
                statements.add(statement);
            }

        });
        //
        try {
            reader.parse(stream, "http://localhost/base/");
        } catch (OpenRDFException ex) {
            throw new ExecutionFacade.OperationFailed("Can't read data.", ex);
        }
        return statements;
    }

    /**
     *
     * @param valueFactory
     * @param execution
     * @return Graph used to store information about execution.
     */
    private static IRI createGraph(ValueFactory valueFactory,
            String execution) {
        return valueFactory.createIRI(execution + "/list");
    }

}
