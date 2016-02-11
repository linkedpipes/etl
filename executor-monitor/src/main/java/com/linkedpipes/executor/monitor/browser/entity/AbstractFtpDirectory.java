package com.linkedpipes.executor.monitor.browser.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 *
 * @author Petr Škoda
 */
public class AbstractFtpDirectory implements FtpFile {


    /**
     * Full ftp path to the directory.
     */
    private final String fileName;

    protected AbstractFtpDirectory(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getAbsolutePath() {
        // Strip the last '/' if necessary.
        String fullName = fileName;
        int fileLength = fullName.length();
        if (fileLength != 1 && fullName.charAt(fileLength - 1) == '/') {
            fullName = fullName.substring(0, fileLength - 1);
        }
        return fullName;
    }

    @Override
    public String getName() {
        // Root - the short name will be '/'.
        if (fileName.equals("/")) {
            return "/";
        }
        // Strip the last '/'.
        String shortName = fileName;
        int filelen = fileName.length();
        if (shortName.charAt(filelen - 1) == '/') {
            shortName = shortName.substring(0, filelen - 1);
        }
        // Return from the last '/'.
        int slashIndex = shortName.lastIndexOf('/');
        if (slashIndex != -1) {
            shortName = shortName.substring(slashIndex + 1);
        }
        return shortName;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean doesExist() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return false;
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
    public int getLinkCount() {
        // It's a directory so it has 3 links.
        return 3;
    }

    @Override
    public long getLastModified() {
        return new Date().getTime();
    }

    @Override
    public boolean setLastModified(long l) {
        return false;
    }

    @Override
    public long getSize() {
        // Return 0 as we represents a directory.
        return 0L;
    }

    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean move(FtpFile ff) {
        return false;
    }

    @Override
    public List<FtpFile> listFiles() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        throw new IOException("No read permission for a directory.");
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        throw new IOException("No read permission for a directory.");
    }

}
