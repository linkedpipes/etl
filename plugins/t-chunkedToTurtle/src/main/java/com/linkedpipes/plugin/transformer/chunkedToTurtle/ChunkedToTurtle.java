package com.linkedpipes.plugin.transformer.chunkedToTurtle;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.ChunkedStatements;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

import java.io.File;

public class ChunkedToTurtle implements Component.Sequential {

    @Component.InputPort(id = "InputChunked")
    public ChunkedStatements inputChunked;

    @Component.InputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Override
    public void execute() throws LpException {
        for (File directory : inputChunked.getSourceDirectories()) {
            outputFiles.addDirectory(directory);
        }
    }

}
