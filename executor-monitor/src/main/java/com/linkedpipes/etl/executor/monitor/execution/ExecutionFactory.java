package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class ExecutionFactory {

    private static final String DEFINITION_FILE =
            "definition" + File.separator + "definition.trig";

    public static void prepareExecutionInDirectory(
            File directory,
            Collection<Statement> pipeline,
            List<MultipartFile> inputs) throws MonitorException {

        // Save pipeline definition.
        File definitionFile = getDefinitionFile(directory);
        definitionFile.getParentFile().mkdirs();
        try (OutputStream stream = new FileOutputStream(definitionFile)) {
            Rio.write(pipeline, stream, RDFFormat.TRIG);
        } catch (IOException | IllegalStateException ex) {
            throw new MonitorException("Can't save pipeline definition.", ex);
        }

        // Save resources.
        if (inputs == null) {
            inputs = Collections.emptyList();
        }
        File inputDirectory = getInputsDirectory(directory);
        for (MultipartFile input : inputs) {
            String originalFileName = input.getOriginalFilename();
            if (originalFileName == null) {
                throw new MonitorException(
                        "Missing original name for: {}", input.getName());
            }
            File targetFile = new File(inputDirectory, originalFileName);
            targetFile.getParentFile().mkdirs();
            try (InputStream inputStream = input.getInputStream()) {
                // Using transferTo throw NoSuchFileException, when
                // the content is saved in memory.
                Files.copy(inputStream, targetFile.toPath());
            } catch (IOException | IllegalStateException ex) {
                throw new MonitorException("Can't prepare inputs.", ex);
            }
        }

    }

    private static File getDefinitionFile(File directory) {
        return new File(directory, DEFINITION_FILE);
    }

    private static File getInputsDirectory(File directory) {
        return new File(directory, "input");
    }

    public static void cloneExecution(File source, File target)
            throws MonitorException {
        File sourceDefinition = getDefinitionFile(source);
        File targetDefinition = getDefinitionFile(target);
        targetDefinition.getParentFile().mkdirs();
        try {
            Files.copy(sourceDefinition.toPath(), targetDefinition.toPath());
        } catch (IOException ex) {
            throw new MonitorException("Can't copy definition.", ex);
        }
        File sourceInputs = getInputsDirectory(source);
        if (!sourceInputs.exists()) {
            return;
        }
        File targetInputs = getInputsDirectory(target);
        try {
            FileUtils.copyDirectory(sourceInputs, targetInputs);
        } catch (IOException ex) {
            throw new MonitorException("Can't copy inputs.", ex);
        }
    }

}
