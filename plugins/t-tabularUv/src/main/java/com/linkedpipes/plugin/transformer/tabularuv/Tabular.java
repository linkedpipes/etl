package com.linkedpipes.plugin.transformer.tabularuv;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dpu.api.Component;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.plugin.transformer.tabularuv.parser.ParserXls;
import com.linkedpipes.plugin.transformer.tabularuv.parser.ParserDbf;
import com.linkedpipes.plugin.transformer.tabularuv.parser.ParserCsv;
import com.linkedpipes.plugin.transformer.tabularuv.mapper.TableToRdf;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.plugin.transformer.tabularuv.parser.ParseFailed;
import com.linkedpipes.plugin.transformer.tabularuv.parser.Parser;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 *
 * @author Škoda Petr
 */
public class Tabular implements SimpleExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Tabular.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(id = "OutputRdf")
    public WritableGraphListDataUnit outputRdf;

    @Component.Configuration
    public TabularConfig_V2 configuration;

    @Component.Inject
    public ProgressReport progressReport;

    public Tabular() {
    }

    @Override
    public void execute(Context context)
            throws NonRecoverableException, ExecutionFailed {
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
                        tableToRdf, context);
                break;
            case DBF:
                parser = new ParserDbf(configuration.getParserDbfConfig(),
                        tableToRdf, context);
                break;
            case XLS:
                parser = new ParserXls(configuration.getParserXlsConfig(),
                        tableToRdf, context);
                break;
            default:
                throw new ExecutionFailed("Unknown table type: {}",
                        configuration.getTableType());
        }
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            if (context.canceled()) {
                throw new ExecutionCancelled();
            }
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
                throw new ExecutionFailed("Can't parse file: {}",
                        entry.getFileName(), ex);
            }
            progressReport.entryProcessed();
        }
        writer.flush();
        progressReport.done();
    }

}
