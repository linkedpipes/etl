package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RdfEncodeHandler extends AbstractRDFHandler {

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
        handler.handleStatement(checkStatement(st));
    }

    private Statement checkStatement(Statement st) {
        statementChanged = false;
        Resource subject = escapeOrReturnNull(st.getSubject());
        IRI predicate = escapeOrReturnNull(st.getPredicate());
        Value object = escapeOrReturnNull(st.getObject());
        Resource context = escapeOrReturnNull(st.getContext());
        if (statementChanged) {
            return valueFactory.createStatement(
                    subject, predicate, object, context);
        } else {
            return st;
        }
    }

    private <T extends Value> T escapeOrReturnNull(T value) {
        if (!(value instanceof IRI)) {
            return value;
        }
        String strValue = value.stringValue();
        StringBuilder builder = new StringBuilder(strValue.length());
        boolean valueChanged = false;
        for (int index = 0; index < strValue.length(); ++index) {
            char character = strValue.charAt(index);
            switch (character) {
                case ' ':
                    builder.append("%20");
                    valueChanged = true;
                    break;
                case '\\':
                    builder.append("%5C");
                    valueChanged = true;
                    break;
                default:
                    builder.append(character);
                    break;
            }
        }
        if (valueChanged) {
            LOG.warn("Invalid value changed: {} -> {}",
                    value.stringValue(), builder.toString());
            statementChanged = true;
            return (T) valueFactory.createIRI(builder.toString());
        } else {
            return value;
        }
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        handler.handleComment(comment);
    }


}

