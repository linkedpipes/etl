package com.linkedpipes.plugin.transformer.shacljena;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ShaclJena implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(ShaclJena.class);

    @Component.InputPort(iri = "Shapes")
    public FilesDataUnit shapesFiles;

    @Component.InputPort(iri = "Data")
    public FilesDataUnit dataFiles;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "Results")
    public WritableFilesDataUnit reportFiles;

    @Component.Configuration
    public ShaclJenaConfiguration configuration;

    /**
     * We use Model not Graph to preserve data about graphs.
     */
    private Model outputModel;

    private boolean shapeFailed = false;

    @Override
    public void execute() throws LpException {
        outputModel = ModelFactory.createDefaultModel();
        List<Shapes> shapes = loadShapes();
        for (FilesDataUnit.Entry fileEntry : dataFiles) {
            for (Shapes shape : shapes) {
                validateDataWithShape(fileEntry, shape);
            }
        }
        addShapes(shapes);
        writeModel();
        checkFailState();
    }

    private List<Shapes> loadShapes() {
        List<Shapes> result = new ArrayList<>();
        for (FilesDataUnit.Entry fileEntry : shapesFiles) {
            Graph content = RDFDataMgr.loadGraph(
                    fileEntry.toFile().getAbsolutePath());
            result.add(Shapes.parse(content));
        }
        return result;
    }

    private void validateDataWithShape(
            FilesDataUnit.Entry fileEntry, Shapes shape) {
        Graph content = RDFDataMgr.loadGraph(
                fileEntry.toFile().getAbsolutePath());
        ValidationReport report = ShaclValidator.get().validate(shape, content);
        outputModel.add(report.getModel());
        if (!report.conforms()) {
            LOG.info(
                    "File '{}' does not conform to shape.",
                    fileEntry.getFileName());
            this.shapeFailed = true;
        }
    }

    private void addShapes(List<Shapes> shapes) {
        if (!configuration.isOutputShapes()) {
            return;
        }
        for (Shapes shape : shapes) {
            var iterator = shape.getGraph().find();
            while (iterator.hasNext()) {
                outputModel.getGraph().add(iterator.next());
            }
        }
    }

    private void writeModel() throws LpException {
        File outputFile = reportFiles.createFile("report.nt");
        try (var stream = new FileOutputStream(outputFile)) {
            RDFDataMgr.write(stream, outputModel, Lang.NTRIPLES);
        } catch (IOException ex) {
            throw new LpException("Can't save report to file.", ex);
        }
    }

    private void checkFailState() throws LpException {
        if (configuration.isFailOnError() && shapeFailed) {
            throw new LpException("Data does not conform to the shapes.");
        }
    }

}
