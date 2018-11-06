package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;

import java.io.File;

public class ResourceManager {

    /**
     * Root directory of all executions.
     */
    private final File root;

    /**
     * Root directory of the execution.
     */
    private final File executionRoot;

    private Integer counter = 0;

    public ResourceManager(File root, File executionRoot) {
        this.root = root;
        this.executionRoot = executionRoot;
    }

    /**
     * Search and return definition file.
     *
     * @return Pipeline as given for execution.
     */
    public File getDefinitionFile() {
        File directory = new File(executionRoot, "definition");
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.startsWith("definition")) {
                return file;
            }
        }
        return null;
    }

    public File getExecutionRoot() {
        return executionRoot;
    }

    /**
     * Resolve relative path in given execution..
     */
    public File resolveExecutionPath(String execution, String path) {
        String executionId = execution.substring(
                execution.indexOf("executions/") + "executions/".length());
        return new File(root, executionId + "/" + path);
    }

    /**
     * Return path to input directory, the directory may not exist.
     */
    public File getInputDirectory() {
        return new File(executionRoot, "input");
    }

    public File getRootWorkingDirectory() {
        return new File(executionRoot, "working");
    }

    /**
     * Return path to working directory, does not create the directory.
     */
    public File getWorkingDirectory(String name) {
        counter += 1;
        return new File(executionRoot, "working/" + name + "-" + counter);
    }

    public File getDebugLogFile() {
        return new File(getExecutionLogDirectory(), "execution-debug.log");
    }

    public File getExecutionLogDirectory() {
        File file = new File(executionRoot, "log");
        file.mkdirs();
        return file;
    }

    public File getWarnLogFile() {
        return new File(getExecutionLogDirectory(), "execution-warn.log");
    }

    /**
     * Return output file for the pipeline execution.
     */
    public File getPipelineFile() {
        File file = new File(executionRoot, "pipeline.trig");
        file.getParentFile().mkdir();
        return file;
    }

    public File getExecutionFile() {
        File file = new File(executionRoot, "execution.trig");
        file.getParentFile().mkdir();
        return file;
    }

    /**
     * Return output file for original execution file.
     */
    public File getComponentMessageFile(ExecutionComponent component) {
        String iri = component.getIri();
        String id = iri.substring(iri.lastIndexOf("/"));
        File file = new File(this.executionRoot, "messages/" + id + ".trig");
        file.getParentFile().mkdir();
        return file;
    }

    public File getPipelineMessageFile() {
        File file = new File(executionRoot, "messages/execution.trig");
        file.getParentFile().mkdir();
        return file;
    }

    public File getOverviewFile() {
        return new File(executionRoot, "execution-overview.jsonld");
    }

    /**
     * Return given path as relative to the execution root directory.
     */
    public String relative(File path) {
        return executionRoot.toPath().relativize(path.toPath()).toString();
    }

}
