package com.linkedpipes.plugin.transformer.tabularuv.dbf;

import org.jamel.dbf.exception.DbfException;

import java.io.*;

public class DbfReaderLanguageDriver implements Closeable {

    private DataInput dataInput;

    private final DbfHeaderLanguageDriver header;

    public DbfReaderLanguageDriver(File file) throws DbfException {
        try {
            dataInput = new RandomAccessFile(file, "r");
            header = DbfHeaderLanguageDriver.read(dataInput);
        } catch (IOException e) {
            throw new DbfException("Cannot open Dbf file " + file, e);
        }
    }

    /**
     * @return DBF header info.
     */
    public DbfHeaderLanguageDriver getHeader() {
        return header;
    }

    @Override
    public void close() {
        try {
            // this method should be idempotent
            if (dataInput instanceof Closeable) {
                ((Closeable) dataInput).close();
                dataInput = null;
            }
        } catch (IOException e) {
            // ignore
        }
    }

}
