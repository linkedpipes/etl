package com.linkedpipes.etl.executor.monitor.debug.ftp;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * Implementation of the FTP server view. Represents a view of the
 * content for client.
 */
public class VirtualFileSystemView implements FileSystemView {

    private final VirtualFileSystem vfs;

    private String currentDirectory;

    public VirtualFileSystemView(VirtualFileSystem virtualFileSystem) {
        this.vfs = virtualFileSystem;
        this.currentDirectory = VirtualFileSystem.ROOT_PATH.getFtpPath();
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        return vfs.getFile(VirtualFileSystem.ROOT_PATH);
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        final VirtualFileSystem.Path path = vfs.resolvePath(currentDirectory);
        if (path == null) {
            return getHomeDirectory();
        } else {
            return vfs.getFile(path);
        }
    }

    @Override
    public boolean changeWorkingDirectory(String ftpPath) throws FtpException {
        final VirtualFileSystem.Path path = vfs.resolvePath(currentDirectory,
                ftpPath);
        if (path == null) {
            return false;
        } else {
            if (path.isSynthetic()) {
                currentDirectory = path.getFtpPath();
                return true;
            } else {
                if (path.getFile().isDirectory()) {
                    currentDirectory = path.getFtpPath();
                    return true;
                } else {
                    // We can't change path to a file.
                    return false;
                }
            }
        }
    }

    @Override
    public FtpFile getFile(String ftpPath) throws FtpException {
        final VirtualFileSystem.Path path = vfs.resolvePath(currentDirectory,
                ftpPath);
        if (path == null) {
            return getHomeDirectory();
        } else {
            return vfs.getFile(path);
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
