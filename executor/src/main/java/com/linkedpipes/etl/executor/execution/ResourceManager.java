package com.linkedpipes.etl.executor.execution;

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
        final File directory = new File(executionRoot, "definition");
        for (File file : directory.listFiles()) {
            final String fileName = file.getName();
            // @TODO Rename to pipeline
            if (fileName.startsWith("definition")) {
                return file;
            }
        }
        return null;
    }

    /**
     * @param execution
     * @param path
     * @return Path to another execution.
     */
    public File resolveExecutionPath(String execution, String path) {
        final String executionId = execution.substring(
                execution.indexOf("executions/") + 11);
        return new File(root, executionId + "/" + path);
    }

    /**
     * @return Path to input directory, the directory may not exist.
     */
    public File getInputDirectory() {
        return new File(executionRoot, "input");
    }

    /**
     * @param name
     * @return Path to working directory, does not create the directory.
     */
    public File getWorkingDirectory(String name) {
        counter += 1;
        final File working = new File(executionRoot,
                "working/" + name + "-" + counter);
        return working;
    }

    public File getExecutionDebugLogFile() {
        final File file = new File(executionRoot, "log/execution.log");
        file.getParentFile().mkdir();
        return file;
    }

    public File getExecutionInfoLogFile() {
        final File file = new File(executionRoot, "log/execution_info.log");
        file.getParentFile().mkdir();
        return file;
    }

    /**
     * @return Output file for the pipeline execution.
     */
    public File getPipelineFile() {
        final File file = new File(executionRoot, "pipeline.trig");
        file.getParentFile().mkdir();
        return file;
    }

    /**
     * @return Output file for original execution file.
     */
    public File getExecutionFileV1() {
        final File file = new File(executionRoot, "execution.jsonld");
        file.getParentFile().mkdir();
        return file;
    }

    /**
     * Return given path as relative to the execution root directory.
     *
     * @param path
     * @return
     */
    public String relative(File path) {
        return executionRoot.toPath().relativize(path.toPath()).toString();
    }

}
