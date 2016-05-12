package com.linkedpipes.etl.executor.monitor.debug.ftp;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * Implementation of the FTP server view. Represents a view of the
 * content for client.
 *
 * @author Petr Škoda
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
//        // Home is always resolved to the synthetic directory.
//        final VirtualFileSystem.ResolvedPath path =
//                virtualFileSystem.resolvePath(VirtualFileSystem.HOME_DIRECTORY, "");
//        return virtualFileSystem.getSyntheticDirectory(path);
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        final VirtualFileSystem.Path path = vfs.resolvePath(currentDirectory);
        if (path == null) {
            return getHomeDirectory();
        } else {
            return vfs.getFile(path);
        }
//        final VirtualFileSystem.ResolvedPath path
//                = virtualFileSystem.resolvePath(currentDirectory, "");
//        if (path == null) {
//            // Path does not exists.
//            return getHomeDirectory();
//        }
//        if (path.isSynthetic()) {
//            return virtualFileSystem.getSyntheticDirectory(path);
//        } else {
//            return new ReadonlyFtpFile(path.getFtpPath(), new File(path.getPath()));
//        }
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
                    // We can't chenge path to a file.
                    return false;
                }
            }
        }
//        final VirtualFileSystem.ResolvedPath path
//                = virtualFileSystem.resolvePath(currentDirectory, ftpPath);
//        if (path == null) {
//            // Invalid path.
//            return false;
//        }
//        if (path.isSynthetic()) {
//            // Synthetic is always directory.
//            currentDirectory = path.getFtpPath();
//            return true;
//        } else {
//            final File fileObject = new File(path.getPath());
//            if (fileObject.isDirectory()) {
//                currentDirectory = path.getFtpPath();
//                return true;
//            } else {
//                // It's not a directory.
//                return false;
//            }
//        }
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
//        final VirtualFileSystem.ResolvedPath path
//                = virtualFileSystem.resolvePath(currentDirectory, ftpPath);
//        if (path == null) {
//            // Path does not exists.
//            return getHomeDirectory();
//        }
//        if (path.isSynthetic()) {
//            return virtualFileSystem.getSyntheticDirectory(path);
//        } else {
//            return new ReadonlyFtpFile(path.getFtpPath(), new File(path.getPath()));
//        }
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
