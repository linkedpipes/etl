package com.linkedpipes.plugin.loader.local;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public final class LoaderLocal implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(LoaderLocal.class);

    @Component.OutputPort(id = "FilesInput")
    public FilesDataUnit input;

    @Component.Configuration
    public LoaderLocalConfiguration configuration;

    @Component.Inject
    public ProgressReport progress;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getPath() == null
                || configuration.getPath().isEmpty()) {
            throw exceptionFactory.missingRdfProperty(
                    LoaderLocalVocabulary.HAS_PATH);
        }
        //
        progress.start(input.size());
        final File rootDirectory = new File(configuration.getPath());
        for (FilesDataUnit.Entry entry : input) {
            //
            final File inputFile = entry.toFile();
            final File outputFile = new File(rootDirectory,
                    entry.getFileName());
            try {
                outputFile.getParentFile().mkdirs();
                Files.copy(inputFile.toPath(), outputFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                LOG.error("{} -> {}", inputFile, outputFile);
                throw exceptionFactory.failure("Can't copy files.", ex);
            }
            //
            progress.entryProcessed();
        }
        progress.done();
    }

}
