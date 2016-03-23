package com.linkedpipes.etl.plugin.transformer.filedecode;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Petr Škoda
 */
public class FileDecode implements SequentialExecution {

    @DataProcessingUnit.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @DataProcessingUnit.OutputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Override
    public void execute(Context context) throws NonRecoverableException {
        for (FilesDataUnit.Entry entry : inputFiles) {
            final File outputFile = outputFiles.createFile(entry.getFileName());
            try (InputStream input = new FileInputStream(entry.getPath())) {
                FileUtils.copyInputStreamToFile(
                        Base64.getDecoder().wrap(input),
                        outputFile);
            } catch (IOException  ex) {
                throw new ExecutionFailed("Can't decode file: {}", entry, ex);
            }
        }
    }

}
