package com.linkedpipes.etl.executor.monitor.execution;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        final List<String> loadPrefLabel = new ArrayList<>(4);
        // Find pipeline subject.
        Resource pipelineResource = null;
        Resource pipelineMetadata = null;
        for (Statement statement : pipelineStatements) {
            if (RDF.TYPE.equals(statement.getPredicate())) {
                if (statement.getObject().stringValue().equals(
                        "http://linkedpipes.com/ontology/Pipeline")) {
                    pipelineResource = statement.getSubject();
                    loadPrefLabel.add(pipelineResource.stringValue());
                } else if (statement.getObject().stringValue().equals(
                        "http://linkedpipes.com/ontology/ExecutionMetadata")) {
                    pipelineMetadata = statement.getSubject();
                }
            }
        }

        if (pipelineResource == null) {
            LOG.error("Missing pipeline, execution: {}", execution);
            execution.setPipelineStatements(Collections.EMPTY_LIST);
        }

        if (pipelineMetadata != null) {
            output.add(valueFactory.createStatement(
                    pipelineMetadata, RDF.TYPE,
                    valueFactory.createIRI(
                            "http://linkedpipes.com/ontology/ExecutionMetadata"),
                    graph));
            output.add(valueFactory.createStatement(
                    pipelineResource,
                    valueFactory.createIRI(
                            "http://linkedpipes.com/ontology/executionMetadata"),
                    pipelineMetadata,
                    graph));
        }

        // Add information from metadata.
        for (Statement statement : pipelineStatements) {
            if (statement.getSubject().equals(pipelineMetadata)) {
                switch (statement.getPredicate().stringValue()) {
                    case "http://linkedpipes.com/ontology/execution/targetComponent":
                        loadPrefLabel.add(statement.getObject().stringValue());
                    case "http://linkedpipes.com/ontology/execution/type":
                        output.add(valueFactory.createStatement(
                                statement.getSubject(),
                                statement.getPredicate(),
                                statement.getObject(), graph));
                        break;
                    default:
                        output.add(valueFactory.createStatement(
                                statement.getSubject(),
                                statement.getPredicate(),
                                statement.getObject(), graph));
                        break;
                }
            }
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

        // Add labels.
        for (Statement statement : pipelineStatements) {
            if (SKOS.PREF_LABEL.equals(statement.getPredicate())) {
                if (loadPrefLabel.contains(
                        statement.getSubject().stringValue())) {
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
     * @param valueFactory
     * @param execution
     * @return Graph used to store information about execution.
     */
    private static IRI createGraph(ValueFactory valueFactory,
            String execution) {
        return valueFactory.createIRI(execution + "/list");
    }

}
