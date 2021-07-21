package com.linkedpipes.plugin.loader.local;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public final class LoaderLocal implements Component, SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(LoaderLocal.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "FilesInput")
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
            throw exceptionFactory.failure("Missing property: {}",
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
        setPermissions(rootDirectory.toPath());
        progress.done();
    }

    private void setPermissions(Path directory) throws LpException {
        String permissionsString = configuration.getPermissions();
        if (permissionsString == null || permissionsString.isBlank()) {
            return;
        }
        Set<PosixFilePermission> permissions =
                PosixFilePermissions.fromString(permissionsString);
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(
                        Path directory, BasicFileAttributes attributes
                ) throws IOException {
                    Files.setPosixFilePermissions(directory, permissions);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(
                        Path file, BasicFileAttributes attributes
                ) throws IOException {
                    Files.setPosixFilePermissions(file, permissions);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException ex) {
            throw new LpException("Can't set file permissions.", ex);
        } catch (UnsupportedOperationException ex) {
            LOG.warn("File system does not support file permissions.");
        }
    }

}
