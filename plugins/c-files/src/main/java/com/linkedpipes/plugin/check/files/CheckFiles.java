package com.linkedpipes.plugin.check.files;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CheckFiles implements Component, SequentialExecution {

    @Component.InputPort(iri = "ExpectedFiles")
    public FilesDataUnit expectedFiles;

    @Component.InputPort(iri = "ActualFiles")
    public FilesDataUnit actualFiles;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        Map<String, File> expected = filesToMap(expectedFiles);
        Map<String, File> actual = filesToMap(actualFiles);
        compareFilesMap(expected, actual);
    }

    private Map<String, File> filesToMap(FilesDataUnit dataUnit) {
        Map<String, File> result = new HashMap<>();
        for (FilesDataUnit.Entry entry : dataUnit) {
            result.put(entry.getFileName(), entry.toFile());
        }
        return result;
    }

    private void compareFilesMap(Map<String, File> expected,
            Map<String, File> actual) throws LpException {
        checkSameSize(expected.size(), actual.size());
        for (Map.Entry<String, File> entry : expected.entrySet()) {
            if (!actual.containsKey(entry.getKey())) {
                throw exceptionFactory.failure("Missing file: {}",
                        entry.getKey());
            }
            if (!filesAreSame(entry.getValue(), actual.get(entry.getKey()))) {
                throw exceptionFactory.failure("Files are not same: {}",
                        entry.getKey());
            }
        }
    }

    private void checkSameSize(int expectedSize, int actualSize)
            throws LpException {
        if (expectedSize != actualSize) {
            throw exceptionFactory.failure("Invalid size: {} {}",
                    expectedSize, actualSize);
        }
    }

    private boolean filesAreSame(File expected, File actual)
            throws LpException {
        try (InputStream expectedStream = new FileInputStream(expected);
             InputStream actualStream = new FileInputStream(actual)) {
            return streamsAreSame(expectedStream, actualStream);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't read files.", ex);
        }
    }

    private boolean streamsAreSame(InputStream expected, InputStream actual)
            throws IOException {
        int expectedValue;
        while ((expectedValue = expected.read()) != -1) {
            int actualValue = actual.read();
            if (expectedValue != actualValue) {
                return false;
            }
        }
        return actual.read() == -1;
    }

}
