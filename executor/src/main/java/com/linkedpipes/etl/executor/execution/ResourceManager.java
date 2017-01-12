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
            if (fileName.startsWith("pipeline")) {
                return file;
            }
        }
        return null;
    }

//    public File resolveExecutionPath(String execution, String path) {
//        final String executionId = execution.substring(
//                execution.indexOf("executions/") + 11);
//        return new File(root, executionId + "/" + path);
//    }

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

    /**
     * @return File with all logs related to the execution.
     */
    public File getExecutionLogFile() {
        final File file = new File(executionRoot, "log/execution.log");
        file.getParentFile().mkdir();
        return file;
    }

    /**
     * @return Path to execution output directory.
     */
    public File getExecutionOutputDirectory() {
        final File file = new File(executionRoot, "execution");
        file.getParentFile().mkdir();
        return file;
    }

//    public File getPipelineFile() {
//        final File file = new File(executionRoot, "pipeline.jsonld");
//        file.getParentFile().mkdir();
//        return file;
//    }
//
//    public File getExecutionFile() {
//        final File file = new File(executionRoot, "execution.jsonld");
//        file.getParentFile().mkdir();
//        return file;
//    }

//    /**
//     * Return given path as relative to the execution root directory.
//     *
//     * @param path
//     * @return
//     */
//    public String relative(File path) {
//        return executionRoot.toPath().relativize(path.toPath()).toString();
//    }

}
