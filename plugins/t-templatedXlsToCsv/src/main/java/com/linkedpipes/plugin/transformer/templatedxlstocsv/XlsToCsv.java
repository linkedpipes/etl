package com.linkedpipes.plugin.transformer.templatedxlstocsv;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import cz.komix.xls2csv.Fact;
import cz.komix.xls2csv.Xls2Csv;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XlsToCsv implements Component, SequentialExecution {

    /**
     * Describe inputs and outputs of a single transformation task.
     */
    private static class Task {

        private File input;

        private File template;

    }

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "Xls")
    public FilesDataUnit inputXls;

    @Component.InputPort(iri = "Templates")
    public FilesDataUnit inputTemplates;

    @Component.OutputPort(iri = "Output")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public XlsToCsvConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        // Templates are prefixed with SABLONA_
        final Map<String, Task> tasks = new HashMap<>();
        for (FilesDataUnit.Entry entry : inputXls) {
            getTask(tasks, entry.getFileName()).input = entry.toFile();
        }
        for (FilesDataUnit.Entry entry : inputTemplates) {
            final String name = transformTemplateName(entry.getFileName());
            getTask(tasks, name).template = entry.toFile();
        }
        // Execute.
        progressReport.start(tasks.size());
        for (Task task : tasks.values()) {
            // Get file name without the extension.
            String fileName = task.input.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            //
            final Xls2Csv xls2Csv = new Xls2Csv(fileName);
            try {
                xls2Csv.init(task.input, task.template);
            } catch (IOException ex) {
                throw new LpException("Can't initialize Xls2Csv.", ex);
            }
            //
            xls2Csv.parse();
            // Iterate output and save.
            for (Fact fact : xls2Csv.getFactBox().getBox()) {
                String outputFile = fact.createFileName(fileName);
                fact.saveToFile(outputFiles.createFile(outputFile),
                        fileName);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    /**
     * Given the name of template return the name of data file.
     *
     * @param name
     * @return
     */
    String transformTemplateName(String name) {
        return name.replace(configuration.getTemplate_prefix(), "");
    }

    static Task getTask(Map<String, Task> tasks, String name) {
        Task task = tasks.get(name);
        if (task == null) {
            task = new Task();
            tasks.put(name, task);
        }
        return task;
    }

}
