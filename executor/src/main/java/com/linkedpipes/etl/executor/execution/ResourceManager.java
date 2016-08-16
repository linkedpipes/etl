package com.linkedpipes.etl.executor.execution;

import java.io.File;

/**
 * @author Škoda Petr
 */
public final class ResourceManager {

    /**
     * Root directory of other executions.
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
     * Path to the definition file.
     *
     * @return
     */
    public File getDefinitionFile() {
        return new File(executionRoot, "definition" + File.separator
                + "definition.jsonld");
    }

    public File resolveExecutionPath(String execution, String path) {
        final String executionId = execution.substring(
                execution.indexOf("executions/") + 11);
        return new File(root, executionId + "/" + path);
    }

    public File getInputDirectory() {
        return new File(executionRoot, "input");
    }

    /**
     * @param name
     * @return Path to working directory, the directory may not exist.
     */
    public File getWorkingDirectory(String name) {
        counter += 1;
        final File working = new File(executionRoot,
                "working/" + name + "-" + counter);
        return working;
    }

    public File getExecutionLogFile() {
        final File file = new File(executionRoot, "log/execution.log");
        file.getParentFile().mkdir();
        return file;
    }

    public File getPipelineFile() {
        final File file = new File(executionRoot, "pipeline.jsonld");
        file.getParentFile().mkdir();
        return file;
    }

    public File getExecutionFile() {
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
