package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

class RdfEncodeHandler extends AbstractRDFHandler {

    private static BitSet allowed = new BitSet(256);

    static {
        // IRIREF ::=  '<' ([^#x00-#x20<>"{}|^`\] | UCHAR)* '>'
        allowed.set(0, 256);
        for (int i = 0; i < 32; ++i) {
            allowed.clear(i);
        }
        allowed.clear('<');
        allowed.clear('>');
        allowed.clear('"');
        allowed.clear('{');
        allowed.clear('}');
        allowed.clear('|');
        allowed.clear('^');
        allowed.clear('`');
        allowed.clear('\\');
    }

    private static final Logger LOG =
            LoggerFactory.getLogger(RdfEncodeHandler.class);

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final RDFHandler handler;

    private boolean statementChanged = false;

    public RdfEncodeHandler(RDFHandler handler) {
        this.handler = handler;
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        handler.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        handler.endRDF();
    }

    @Override
    public void handleNamespace(String prefix, String uri)
            throws RDFHandlerException {
        handler.handleNamespace(prefix, uri);
    }

    @Override
    public void handleStatement(Statement st)
            throws RDFHandlerException {
        try {
            handler.handleStatement(checkStatement(st));
        } catch (UnsupportedEncodingException ex) {
            throw new RDFHandlerException(ex);
        }
    }

    private Statement checkStatement(Statement st)
            throws UnsupportedEncodingException {
        statementChanged = false;
        Resource subject = makeSave(st.getSubject());
        IRI predicate = makeSave(st.getPredicate());
        Value object = makeSave(st.getObject());
        Resource context = makeSave(st.getContext());
        if (statementChanged) {
            return valueFactory.createStatement(
                    subject, predicate, object, context);
        } else {
            return st;
        }
    }

    private <T extends Value> T makeSave(T value)
            throws UnsupportedEncodingException {
        if (!(value instanceof IRI)) {
            return value;
        }
        //
        String iriAsString = value.stringValue();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(
                iriAsString.length() + 6);
        // Inspired by URLCodec.encodeUrl
        boolean valueChanged = false;
        for (byte c : iriAsString.getBytes("UTF-8")) {
            int b = c;
            if (b < 0) {
                b = 256 + b;
            }
            if (allowed.get(b)) {
                // Special handling of a space, we do not use '+'.
                if (b == ' ') {
                    valueChanged = true;
                    buffer.write('%');
                    buffer.write('2');
                    buffer.write('0');
                } else {
                    buffer.write(b);
                }
            } else {
                valueChanged = true;
                buffer.write('%');
                char hex1 = Character.toUpperCase(
                        Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = Character.toUpperCase(
                        Character.forDigit(b & 0xF, 16));
                buffer.write(hex1);
                buffer.write(hex2);
            }
        }
        if (valueChanged) {
            String encoded = new String(buffer.toByteArray(), "UTF-8");
            LOG.warn("Invalid value changed: {} -> {}",
                    iriAsString, encoded);
            statementChanged = true;
            return (T) valueFactory.createIRI(encoded);
        } else {
            return value;
        }
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        handler.handleComment(comment);
    }

}
