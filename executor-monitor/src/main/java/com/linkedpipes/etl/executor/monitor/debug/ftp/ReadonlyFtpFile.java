package com.linkedpipes.etl.executor.monitor.debug.ftp;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * Represents an abstract file.
 *
 * @author Petr Škoda
 */
class ReadonlyFtpFile extends NativeFtpFile {

    ReadonlyFtpFile(String ftpPath, File file) {
        super(ftpPath, file, null);
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public String getOwnerName() {
        return "user";
    }

    @Override
    public String getGroupName() {
        return "group";
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean move(FtpFile dest) {
        return false;
    }

    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public List<FtpFile> listFiles() {
        final File file = getPhysicalFile();
        if (!file.isDirectory()) {
            return null;
        }
        // List files in directory.
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        // Sort.
        Arrays.sort(files, (File f1, File f2) -> f1.getName().compareTo(f2.getName()));
        // Get the virtual name of the base directory.
        String virtualFileStr = getAbsolutePath();
        if (virtualFileStr.charAt(virtualFileStr.length() - 1) != '/') {
            virtualFileStr += '/';
        }
        // Construct representation of files.
        FtpFile[] virtualFiles = new FtpFile[files.length];
        for (int i = 0; i < files.length; ++i) {
            File fileObj = files[i];
            String fileName = virtualFileStr + fileObj.getName();
            virtualFiles[i] = new ReadonlyFtpFile(fileName, fileObj);
        }
        return Collections.unmodifiableList(Arrays.asList(virtualFiles));
    }

}
