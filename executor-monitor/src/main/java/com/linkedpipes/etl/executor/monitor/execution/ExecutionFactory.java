package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

class ExecutionFactory {

    public static void prepareExecutionInDirectory(
            File directory,
            Collection<Statement> pipeline,
            List<MultipartFile> inputs) throws MonitorException {

        // Save pipeline definition.
        File definitionFile = new File(
                directory, "definition" + File.separator + "definition.trig");
        definitionFile.getParentFile().mkdirs();
        try (OutputStream stream = new FileOutputStream(definitionFile)) {
            Rio.write(pipeline, stream, RDFFormat.TRIG);
        } catch (IOException | IllegalStateException ex) {
            throw new MonitorException("Can't save pipeline definition.", ex);
        }

        // Save resources.
        File inputDirectory = new File(directory, "input");
        for (MultipartFile input : inputs) {
            String originalFileName = input.getOriginalFilename();
            if (originalFileName == null) {
                throw new MonitorException(
                        "Missing original name for: {}", input.getName());
            }
            File inputFile = new File(inputDirectory, originalFileName);
            inputFile.getParentFile().mkdirs();
            try {
                input.transferTo(inputFile);
            } catch (IOException | IllegalStateException ex) {
                throw new MonitorException("Can't prepare inputs.", ex);
            }
        }

    }

}
