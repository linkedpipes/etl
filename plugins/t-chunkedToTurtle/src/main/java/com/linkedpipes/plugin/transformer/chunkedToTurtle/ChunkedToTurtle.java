package com.linkedpipes.plugin.transformer.chunkedToTurtle;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ChunkedToTurtle implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputChunked")
    public ChunkedTriples inputChunked;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Override
    public void execute() throws LpException {
        for (File directory : inputChunked.getSourceDirectories()) {
            for (File file : directory.listFiles()) {
                final File destination = outputFiles.createFile(file.getName());
                try {
                    Files.copy(file.toPath(), destination.toPath());
                } catch (IOException ex) {
                    throw new LpException("Can't copy data file.");
                }
            }
        }
    }

}
