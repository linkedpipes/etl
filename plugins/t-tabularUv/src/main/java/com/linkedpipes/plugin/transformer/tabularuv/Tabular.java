package com.linkedpipes.plugin.transformer.tabularuv;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.plugin.transformer.tabularuv.mapper.TableToRdf;
import com.linkedpipes.plugin.transformer.tabularuv.parser.*;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tabular implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Tabular.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableGraphListDataUnit outputRdf;

    @Component.Configuration
    public TabularConfig_V2 configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final RdfWriter writer = new RdfWriter(outputRdf);
        final TableToRdf tableToRdf = new TableToRdf(
                configuration.getTableToRdfConfig(),
                writer,
                valueFactory);
        // Prepare parser based on type.
        final Parser parser;
        switch (configuration.getTableType()) {
            case CSV:
                parser = new ParserCsv(configuration.getParserCsvConfig(),
                        tableToRdf);
                break;
            case DBF:
                parser = new ParserDbf(configuration.getParserDbfConfig(),
                        tableToRdf);
                break;
            case XLS:
                parser = new ParserXls(configuration.getParserXlsConfig(),
                        tableToRdf);
                break;
            default:
                throw new LpException("Unknown table type: {}",
                        configuration.getTableType());
        }
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            final IRI outputGraph = outputRdf.createGraph();
            writer.setGraph(outputGraph);
            LOG.info("Processing: {}", entry.getFileName());
            // If set add subject for the whole table.
            if (configuration.isUseTableSubject()) {
                // Prepare subject for table.
                final IRI tableSubject = valueFactory.createIRI(
                        entry.toFile().toURI().toString());
                tableToRdf.setTableSubject(tableSubject);
                writer.add(tableSubject,
                        TabularOntology.TABLE_SYMBOLIC_NAME,
                        valueFactory.createLiteral(entry.getFileName()));
            }
            // Parse file.
            try {
                parser.parse(entry.toFile());
            } catch (ParseFailed ex) {
                throw new LpException("Can't parse file: {}",
                        entry.getFileName(), ex);
            }
            progressReport.entryProcessed();
        }
        writer.flush();
        progressReport.done();
    }

}
