package com.linkedpipes.executor.monitor.browser.entity;

import java.util.List;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * Represent a root directory.
 *
 * @author Petr Škoda
 */
public final class RootFtpDirectory extends AbstractFtpDirectory {

    private final VirtualFileSystem fileSystem;

    public RootFtpDirectory(String fileName, VirtualFileSystem fileSystem) {
        super(fileName);
        this.fileSystem = fileSystem;
    }

    @Override
    public List<FtpFile> listFiles() {
        return fileSystem.getDirectoryContent(this);
    }

}
