package com.linkedpipes.plugin.transformer.hdtToRdf;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.n3.N3ParserFactory;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class HdtToRdf implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(HdtToRdf.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public HdtToRdfConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private BufferedWriter writer;

    private final N3ParserFactory parserFactory = new N3ParserFactory();

    @Override
    public void execute() throws LpException {
        prepareStatementWriter();
        loadFiles();
    }

    private void prepareStatementWriter() {
        writer = new BufferedWriter(configuration.getCommitSize(), outputRdf);
    }

    private void loadFiles() throws LpException {
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            loadEntry(entry);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void loadEntry(FilesDataUnit.Entry entry) throws LpException {
        RDFParser parser = parserFactory.getParser();
        parser.setRDFHandler(writer);
        try {
            HDT hdt = HDTManager.loadHDT(entry.toFile().toString());
            LOG.info("Converting {} triples ...", hdt.size());
            HdtN3Reader reader = new HdtN3Reader(hdt.search("", "", ""));
            parser.parse(reader, "http://localhost/base/");
            LOG.info("Converting {} triples ... done", hdt.size());
        } catch (IOException ex) {
            throw new LpException(
                    "Can't read file: {}", entry.getFileName(), ex);
        } catch (NotFoundException ex) {
            // This is ok, as if no triples were found there is just no output.
        }
    }

}
