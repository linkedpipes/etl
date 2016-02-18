package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit.Entry;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class Tabular implements SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Tabular.class);

    @DataProcessingUnit.InputPort(id = "InputFiles")
    public FilesDataUnit inputFilesDataUnit;

    @DataProcessingUnit.OutputPort(id = "OutputRdf")
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
            output.onFileStart();
            mapper.onTableStart("file://" + entry.getFileName(), null);
            try {
                parser.parse(entry, mapper, context);
            } catch (IOException ex) {
                throw new DataProcessingUnit.ExecutionFailed("Can't process file: " + entry.getFileName(), ex);
            }
            mapper.onTableEnd();
            output.onFileEnd();
        }
    }

}
