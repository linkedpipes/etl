package com.linkedpipes.executor.execution.contoller;

import java.io.File;


/**
 *
 * @author Škoda Petr
 */
public final class ResourceManager {

    private final File root;

    private final File definitionDir;

    private final File workingDir;

    private final File dumpDir;

    private final File systemDir;

    private final File monitorDir;

    private final File definitionRepositoryDir;

    private final File messageRepositoryDir;

    private final File systemLogFile;

    private final File definitionDumpFile;

    private final File messageDumpFile;

    private final File loggingDir;

    private final File statusFile;

    private final File debugFile;

    /**
     * Dump of all labels from pipeline definition with language tags.
     */
    private final File labelDumpFile;

    private int workingDirectoryCounter = 0;

    public ResourceManager(File root) {
        this.root = root;
        this.definitionDir = createDirectory("definition");
        this.workingDir = createDirectory("working");
        this.dumpDir = createDirectory("dump");
        this.loggingDir = createDirectory(dumpDir, "log");
        this.definitionDumpFile = new File(dumpDir, "definition.jsonld");
        this.messageDumpFile = new File(dumpDir, "messages.jsonld");
        this.labelDumpFile = new File(dumpDir, "labels.json");
        this.systemDir = createDirectory("system");
        this.definitionRepositoryDir = createDirectory(systemDir, "repository/definition/");
        this.messageRepositoryDir = createDirectory(systemDir, "repository/service/");
        this.systemLogFile = new File(systemDir, "pipeline.log");
        this.monitorDir = createDirectory("monitor");
        this.statusFile = new File(monitorDir, "status.json");
        this.debugFile = new File(dumpDir, "debug.json");
    }

    /**
     * Create directory in {@link #root} and return {@link File} that points to it.
     *
     * @param subPath
     * @return
     */
    private File createDirectory(String subPath) {
        return createDirectory(root, subPath);
    }

    private File createDirectory(File rootPath, String subPath) {
        final File output = new File(rootPath, subPath);
        output.mkdirs();
        return output;
    }

    /**
     *
     * @return Root of execution directory.
     */
    public File getRoot() {
        return root;
    }

    public File getDefinitionDirectory() {
        return definitionDir;
    }

    public File getDefinitionRepositryDir() {
        return definitionRepositoryDir;
    }

    public File getMessageRepositryDir() {
        return messageRepositoryDir;
    }

    public File getSystemLogFile() {
        return systemLogFile;
    }

    public File getDefinitionDumpFile() {
        return definitionDumpFile;
    }

    public File getMessageDumpFile() {
        return messageDumpFile;
    }

    public File getWorkingDir() {
        return getWorkingDir(Integer.toString(++workingDirectoryCounter));
    }

    public File getWorkingDir(String name) {
        final File output = new File(workingDir, name);
        output.mkdirs();
        return output;
    }

    public File getComponentLogFile(String componentId) {
        final File output = new File(loggingDir, componentId);
        output.mkdirs();
        return new File(output, "log.ttl");
    }

    public File getStatusFile() {
        return statusFile;
    }

    public File getLabelDumpFile() {
        return labelDumpFile;
    }

    public File getDebugFile() {
        return debugFile;
    }

}
