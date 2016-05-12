package com.linkedpipes.plugin.transformer.tabularuv.dbf;

import java.io.DataInput;
import java.io.IOException;

import org.jamel.dbf.exception.DbfException;
import org.jamel.dbf.structure.DbfHeader;
import org.jamel.dbf.utils.DbfUtils;

public class DbfHeaderLanguageDriver extends DbfHeader {

    private byte languageDriver;

    public static DbfHeaderLanguageDriver read(DataInput dataInput)
            throws DbfException {
        try {
            final DbfHeaderLanguageDriver header
                    = new DbfHeaderLanguageDriver();
            dataInput.readByte(); // 0
            dataInput.readByte(); // 1
            dataInput.readByte(); // 2
            dataInput.readByte(); // 3
            DbfUtils.readLittleEndianInt(dataInput); // 4-7
            DbfUtils.readLittleEndianShort(dataInput); // 8-9
            DbfUtils.readLittleEndianShort(dataInput); // 10-11
            DbfUtils.readLittleEndianShort(dataInput); // 12-13
            dataInput.readByte(); // 14
            dataInput.readByte(); // 15
            DbfUtils.readLittleEndianInt(dataInput); // 16-19
            dataInput.readInt(); // 20-23
            dataInput.readInt(); // 24-27
            dataInput.readByte(); // 28
            header.languageDriver = dataInput.readByte(); // 29
            return header;
        } catch (IOException e) {
            throw new DbfException("Cannot read Dbf header", e);
        }
    }

    public byte getLanguageDriver() {
        return languageDriver;
    }

}
