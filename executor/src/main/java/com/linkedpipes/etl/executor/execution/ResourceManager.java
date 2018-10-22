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
        for (File file : directory.listFiles()) {
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
     * @param execution
     * @param path
     * @return Path to another execution.
     */
    public File resolveExecutionPath(String execution, String path) {
        String executionId = execution.substring(
                execution.indexOf("executions/") + 11);
        return new File(root, executionId + "/" + path);
    }

    /**
     * @return Path to input directory, the directory may not exist.
     */
    public File getInputDirectory() {
        return new File(executionRoot, "input");
    }

    public File getRootWorkingDirectory() {
        return new File(executionRoot, "working");
    }

    /**
     * @param name
     * @return Path to working directory, does not create the directory.
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
     * @return Output file for the pipeline execution.
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
     * @return Output file for original execution file.
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
