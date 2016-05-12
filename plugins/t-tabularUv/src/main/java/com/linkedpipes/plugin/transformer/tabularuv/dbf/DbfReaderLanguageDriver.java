package com.linkedpipes.plugin.transformer.tabularuv.dbf;

import java.io.Closeable;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jamel.dbf.exception.DbfException;

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
