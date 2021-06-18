package com.linkedpipes.plugin.transformer.filesToRdf;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import java.util.Date;

/**
 * Make sure that each blank node is prefixed by a time when the file is parsed
 * and given prefix. As a result blanks nodes from different files should
 * not collide.
 */
class BlankNodePrefixUpdater implements RDFHandler {

    private final RDFHandler handler;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private String filePrefix;

    private final String prefix;

    public BlankNodePrefixUpdater(RDFHandler writer, String identifier) {
        this.handler = writer;
        this.prefix = identifier;
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        handler.startRDF();
        filePrefix = prefix + "_" + (new Date()).getTime() + "_";
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
    public void handleStatement(Statement st) throws RDFHandlerException {
        Resource subject = st.getSubject();
        Value object = st.getObject();
        boolean updated = false;
        if (subject instanceof BNode) {
            subject = prefixBlankNode((BNode) subject);
            updated = true;
        }
        if (object instanceof BNode) {
            object = prefixBlankNode((BNode) object);
            updated = true;
        }
        if (updated) {
            handler.handleStatement(valueFactory.createStatement(subject,
                    st.getPredicate(), object, st.getContext()));
        } else {
            handler.handleStatement(st);
        }
    }

    private BNode prefixBlankNode(BNode node) {
        return valueFactory.createBNode(filePrefix + node.getID());
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        handler.handleComment(comment);
    }

}
