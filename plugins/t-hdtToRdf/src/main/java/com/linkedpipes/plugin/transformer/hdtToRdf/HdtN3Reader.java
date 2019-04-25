package com.linkedpipes.plugin.transformer.hdtToRdf;

import org.rdfhdt.hdt.triples.IteratorTripleString;

import java.io.IOException;
import java.io.Reader;

public class HdtN3Reader extends Reader {

    private final IteratorTripleString iterator;

    private CharSequence line = "".subSequence(0, 0);

    private int pos = 0;

    public HdtN3Reader(IteratorTripleString iterator) {
        this.iterator = iterator;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        for (int index = off; index < off + len; ++index) {
            Character nextChar = getNextChar();
            if (nextChar == null) {
                if (index == off) {
                    // We have read nothing.
                    return -1;
                }
                return index - off;
            }
            cbuf[index] = nextChar;
        }
        return len;
    }

    protected Character getNextChar() throws IOException {
        if (pos + 1 >= line.length()) {
            if (!readNextTriple()) {
                return null;
            }
        }
        return line.charAt(pos++);
    }

    protected Boolean readNextTriple() throws IOException {
        if (!iterator.hasNext()) {
            return false;
        }
        line = iterator.next().asNtriple();
        pos = 0;
        return true;
    }

    @Override
    public void close() {
        // No operation here.
    }

}
