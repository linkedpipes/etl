package com.linkedpipes.etl.executor.execution;

import java.io.File;

/**
 *
 * @author Škoda Petr
 */
public final class ResourceManager {

    private final File root;

    private Integer counter = 0;

    public ResourceManager(File root) {
        this.root = root;
    }

    /**
     * Path to the definition file.
     *
     * @return
     */
    public File getDefinitionFile() {
        return new File(root, "definition" + File.separator
                + "definition.jsonld");
    }

    public File resolveExecutionPath(String execution, String parh) {
        throw new UnsupportedOperationException();
    }

    public File getWorkingDirectory(String name) {
        counter += 1;
        final File working = new File(root, "working/" + name + "-" + counter);
        working.mkdirs();
        return working;
    }

    public File getExecutionLogFile() {
        final File file = new File(root, "log/execution.log");
        file.getParentFile().mkdir();
        return file;
    }

    public File getPipelineFile() {
        final File file = new File(root, "pipeline.jsonld");
        file.getParentFile().mkdir();
        return file;
    }

    public File getExecutionFile() {
        final File file = new File(root, "execution.jsonld");
        file.getParentFile().mkdir();
        return file;
    }

    public String relativize(File path) {
        return root.toPath().relativize(path.toPath()).toString();
    }

}
