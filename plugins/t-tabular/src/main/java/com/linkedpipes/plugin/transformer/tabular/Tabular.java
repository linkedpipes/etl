package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit.Entry;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;

/**
 *
 * @author Petr Škoda
 */
public class Tabular implements SimpleExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Tabular.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFilesDataUnit;

    @Component.OutputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdfDataUnit;

    @Configuration
    public TabularConfiguration configuration;

    @Override
    public void execute(Context context) throws NonRecoverableException, ExecutionCancelled {
        final BufferedOutput output = new BufferedOutput(outputRdfDataUnit);
        final Parser parser = new Parser(configuration);
        final Mapper mapper = new Mapper(output, configuration, ColumnFactory.createColumnList(configuration));
        // TODO We could use some table group URI from user?
        mapper.initialize(null);
        for (Entry entry : inputFilesDataUnit) {
            LOG.info("Processing file: {}", entry.toFile());
            output.onFileStart();
            mapper.onTableStart("file://" + entry.getFileName(), null);
            try {
                parser.parse(entry, mapper, context);
            } catch (IOException ex) {
                throw new Component.ExecutionFailed("Can't process file: " + entry.getFileName(), ex);
            }
            mapper.onTableEnd();
            output.onFileEnd();
        }
    }

}
