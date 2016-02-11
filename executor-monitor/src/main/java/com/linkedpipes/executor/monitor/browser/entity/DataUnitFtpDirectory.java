package com.linkedpipes.executor.monitor.browser.entity;

import java.util.List;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * Represent a directory of data unit.
 *
 * @author Petr Škoda
 */
public final class DataUnitFtpDirectory extends AbstractFtpDirectory {

    private final VirtualFileSystem.DataUnitDirectory dataUnitDirectory;

    private final VirtualFileSystem fileSystem;

    public DataUnitFtpDirectory(String fileName, VirtualFileSystem fileSystem,
            VirtualFileSystem.DataUnitDirectory dataUnitDirectory) {
        super(fileName);
        this.dataUnitDirectory = dataUnitDirectory;
        this.fileSystem = fileSystem;
    }

    public VirtualFileSystem.DataUnitDirectory getDataUnitDirectory() {
        return dataUnitDirectory;
    }

    @Override
    public List<FtpFile> listFiles() {
        return fileSystem.getDirectoryContent(this);
    }

}
