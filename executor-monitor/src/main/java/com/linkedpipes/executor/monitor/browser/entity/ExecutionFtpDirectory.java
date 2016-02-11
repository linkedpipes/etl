package com.linkedpipes.executor.monitor.browser.entity;

import java.util.List;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * Represent a directory of execution.
 *
 * @author Petr Škoda
 */
public final class ExecutionFtpDirectory extends AbstractFtpDirectory {

    private final VirtualFileSystem.ExecutionDirectory executionDirectory;

    private final VirtualFileSystem fileSystem;

    public ExecutionFtpDirectory(String fileName, VirtualFileSystem fileSystem,
            VirtualFileSystem.ExecutionDirectory executionDirectory) {
        super(fileName);
        this.executionDirectory = executionDirectory;
        this.fileSystem = fileSystem;
    }

    public VirtualFileSystem.ExecutionDirectory getExecutionDirectory() {
        return executionDirectory;
    }

    @Override
    public List<FtpFile> listFiles() {
        return fileSystem.getDirectoryContent(this);
    }

}
