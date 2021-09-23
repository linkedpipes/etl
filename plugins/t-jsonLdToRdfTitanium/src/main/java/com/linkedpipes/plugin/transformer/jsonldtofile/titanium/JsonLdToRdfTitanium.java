package com.linkedpipes.plugin.transformer.jsonldtofile.titanium;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.api.ToRdfApi;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfLiteral;
import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.RdfValue;
import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JsonLdToRdfTitanium implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(JsonLdToRdfTitanium.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public JsonLdToRdfTitaniumConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() throws LpException {
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            try {
                loadFile(entry);
            } catch (LpException ex) {
                if (configuration.isSkipOnFailure()) {
                    LOG.error("Can't load file: {}", entry.getFileName());
                } else {
                    throw ex;
                }
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void loadFile(FilesDataUnit.Entry entry) throws LpException {
        JsonDocument jsonDocument;
        try (InputStream stream = new FileInputStream(entry.toFile())) {
            jsonDocument = JsonDocument.of(stream);
        } catch (IOException | JsonLdError ex) {
            throw new LpException(
                    "Can't load file as JSON.", entry.getFileName(), ex);
        }
        ToRdfApi toRdfApi = new ToRdfApi(jsonDocument);
        RdfDataset rdfDocument;
        try {
            rdfDocument = toRdfApi.get();
        } catch (JsonLdError ex) {
            throw new LpException(
                    "Can't convert JSON to JSON-LD.",
                    entry.getFileName(), ex);
        }
        List<Statement> statements = new ArrayList<>();
        for (RdfNQuad rdfNQuad : rdfDocument.toList()) {
            statements.add(asRdf4jStatement(rdfNQuad));
        }
        IRI graph = outputRdf.getWriteGraph();
        outputRdf.execute((connection -> {
            connection.add(statements, graph);
        }));
    }

    private Statement asRdf4jStatement(RdfNQuad quad) throws LpException {
        return valueFactory.createStatement(
                asRdf4jResource(quad.getSubject()),
                asRdf4jIri(quad.getPredicate()),
                asRdf4jValue(quad.getObject())
        );
    }

    private Resource asRdf4jResource(RdfResource rdfResource)
            throws LpException {
        String value = rdfResource.getValue();
        if (rdfResource.isIRI()) {
            return valueFactory.createIRI(value);
        }
        if (rdfResource.isBlankNode()) {
            return valueFactory.createBNode(value);
        }
        throw new LpException("Can not cast '{}' to resource.", value);

    }

    private IRI asRdf4jIri(RdfResource rdfResource) throws LpException {
        String value = rdfResource.getValue();
        if (rdfResource.isIRI()) {
            return valueFactory.createIRI(value);
        }
        throw new LpException("Can not cast '{}' to IRI.", value);
    }

    private Value asRdf4jValue(RdfValue rdfValue) throws LpException {
        String value = rdfValue.getValue();
        if (rdfValue.isIRI()) {
            return valueFactory.createIRI(value);
        }
        if (rdfValue.isBlankNode()) {
            return valueFactory.createBNode(value);
        }
        if (rdfValue.isLiteral()) {
            RdfLiteral literal = (RdfLiteral) rdfValue;
            String type = literal.getDatatype();
            Optional<String> language = literal.getLanguage();
            if (language.isPresent()) {
                return valueFactory.createLiteral(value, language.get());
            }
            return valueFactory.createLiteral(value,
                    valueFactory.createIRI(type));
        }
        throw new LpException("Can not cast '{}' to value.", value);

    }

}
