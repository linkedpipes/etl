package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Chunked version of tabular.
 */
public class TabularChunked implements Component, SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(TabularChunked.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFilesDataUnit;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdfDataUnit;

    @Component.Configuration
    public TabularConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        final RdfOutput output = new RdfOutput(outputRdfDataUnit,
                configuration.getChunkSize());
        LOG.info("Chunk size: {}", configuration.getChunkSize());
        final Parser parser = new Parser(configuration);
        final Mapper mapper = new Mapper(output, configuration,
                ColumnFactory.createColumnList(configuration));
        mapper.initialize(null);
        progressReport.start(inputFilesDataUnit.size());
        for (FilesDataUnit.Entry entry : inputFilesDataUnit) {
            output.onFileStart();
            final String table;
            switch (configuration.getEncodeType()) {
                case "emptyHost":
                    table = "file:///" + entry.getFileName();
                    break;
                default:
                    table = "file://" + entry.getFileName();
                    break;
            }
            mapper.onTableStart(table, null);
            try {
                parser.parse(entry, mapper);
            } catch (IOException | ColumnAbstract.MissingColumnValue ex) {
                throw new LpException("Can't process file: {}",
                        entry.getFileName(), ex);
            }
            mapper.onTableEnd();
            output.onFileEnd();
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
