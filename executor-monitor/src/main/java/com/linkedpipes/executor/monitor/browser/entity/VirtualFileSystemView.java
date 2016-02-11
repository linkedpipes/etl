package com.linkedpipes.executor.monitor.browser.entity;

import java.io.File;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * Represents an abstract file system.
 *
 * @author Petr Škoda
 */
public class VirtualFileSystemView implements FileSystemView {

    private final VirtualFileSystem virtualFileSystem;

    private String currentDirectory;

    public VirtualFileSystemView(VirtualFileSystem virtualFileSystem) {
        this.virtualFileSystem = virtualFileSystem;
        this.currentDirectory = "/";
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        // Home is always resolved to the synthetic directory.
        final VirtualFileSystem.ResolvedPath path = virtualFileSystem.resolvePath(VirtualFileSystem.HOME_DIRECTORY, "");
        return virtualFileSystem.getSyntheticDirectory(path);
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        final VirtualFileSystem.ResolvedPath path = virtualFileSystem.resolvePath(currentDirectory, "");
        if (path == null) {
            // Path does not exists.
            return getHomeDirectory();
        }
        if (path.isSynthetic()) {
            return virtualFileSystem.getSyntheticDirectory(path);
        } else {
            return new ReadonlyFtpFile(path.getFtpPath(), new File(path.getPath()));
        }
    }

    @Override
    public boolean changeWorkingDirectory(String ftpPath) throws FtpException {
        final VirtualFileSystem.ResolvedPath path = virtualFileSystem.resolvePath(currentDirectory, ftpPath);
        if (path == null) {
            // Invalid path.
            return false;
        }
        if (path.isSynthetic()) {
            // Synthetic is always directory.
            currentDirectory = path.getFtpPath();
            return true;
        } else {
            final File fileObject = new File(path.getPath());
            if (fileObject.isDirectory()) {
                currentDirectory = path.getFtpPath();
                return true;
            } else {
                // It's not a directory.
                return false;
            }
        }
    }

    @Override
    public FtpFile getFile(String ftpPath) throws FtpException {
        final VirtualFileSystem.ResolvedPath path = virtualFileSystem.resolvePath(currentDirectory, ftpPath);
        if (path == null) {
            // Path does not exists.
            return getHomeDirectory();
        }
        if (path.isSynthetic()) {
            return virtualFileSystem.getSyntheticDirectory(path);
        } else {
            return new ReadonlyFtpFile(path.getFtpPath(), new File(path.getPath()));
        }
    }

    @Override
    public boolean isRandomAccessible() throws FtpException {
        return true;
    }

    @Override
    public void dispose() {
        // No operation here.
    }

}
