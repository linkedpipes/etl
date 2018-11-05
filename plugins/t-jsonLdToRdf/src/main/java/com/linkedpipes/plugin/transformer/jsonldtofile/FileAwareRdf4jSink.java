package com.linkedpipes.plugin.transformer.jsonldtofile;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.semarglproject.rdf.ParseException;
import org.semarglproject.rdf4j.core.sink.RDF4JSink;
import org.semarglproject.sink.QuadSink;
import org.semarglproject.vocab.RDF;

import java.util.Date;

public class FileAwareRdf4jSink implements QuadSink {

    protected RDFHandler handler;

    protected ValueFactory valueFactory;

    protected String blankNodeFilePrefix;

    protected FileAwareRdf4jSink(RDFHandler handler) {
        this.valueFactory = SimpleValueFactory.getInstance();
        this.handler = handler;
        this.blankNodeFilePrefix = "t-" + (new Date()).getTime() + "-";
    }

    public static FileAwareRdf4jSink connect(RDFHandler handler) {
        return new FileAwareRdf4jSink(handler);
    }

    protected Resource convertNonLiteral(String arg) {
        if (arg.startsWith(RDF.BNODE_PREFIX)) {
            return valueFactory.createBNode(
                    blankNodeFilePrefix + arg.substring(2));
        }
        return valueFactory.createIRI(arg);
    }

    @Override
    public final void addNonLiteral(String s, String p, String o) {
        addTriple(
                convertNonLiteral(s),
                valueFactory.createIRI(p),
                convertNonLiteral(o));
    }

    @Override
    public final void addPlainLiteral(
            String s, String p, String content, String lang) {
        if (lang == null) {
            addTriple(
                    convertNonLiteral(s),
                    valueFactory.createIRI(p),
                    valueFactory.createLiteral(content));
        } else {
            addTriple(
                    convertNonLiteral(s),
                    valueFactory.createIRI(p),
                    valueFactory.createLiteral(content, lang));
        }
    }

    @Override
    public final void addTypedLiteral(
            String s, String p, String content, String type) {
        Literal literal = valueFactory.createLiteral(
                content, valueFactory.createIRI(type));
        addTriple(convertNonLiteral(s), valueFactory.createIRI(p), literal);
    }

    protected void addTriple(Resource s, IRI p, Value o) {
        try {
            handler.handleStatement(valueFactory.createStatement(s, p, o));
        } catch (RDFHandlerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void addNonLiteral(String s, String p, String o, String graph) {
        if (graph == null) {
            addNonLiteral(s, p, o);
        } else {
            addQuad(
                    convertNonLiteral(s),
                    valueFactory.createIRI(p),
                    convertNonLiteral(o),
                    convertNonLiteral(graph));
        }
    }

    @Override
    public final void addPlainLiteral(
            String s, String p, String content, String lang, String graph) {
        if (graph == null) {
            addPlainLiteral(s, p, content, lang);
        } else {
            if (lang == null) {
                addQuad(
                        convertNonLiteral(s),
                        valueFactory.createIRI(p),
                        valueFactory.createLiteral(content),
                        convertNonLiteral(graph));
            } else {
                addQuad(
                        convertNonLiteral(s),
                        valueFactory.createIRI(p),
                        valueFactory.createLiteral(content, lang),
                        convertNonLiteral(graph));
            }
        }
    }

    @Override
    public final void addTypedLiteral(
            String s, String p, String content, String type, String graph) {
        if (graph == null) {
            addTypedLiteral(s, p, content, type);
        } else {
            Literal literal = valueFactory.createLiteral(
                    content, valueFactory.createIRI(type));
            addQuad(
                    convertNonLiteral(s),
                    valueFactory.createIRI(p),
                    literal,
                    convertNonLiteral(graph));
        }
    }

    protected void addQuad(Resource s, IRI p, Value o, Resource graph) {
        try {
            handler.handleStatement(
                    valueFactory.createStatement(s, p, o, graph));
        } catch (RDFHandlerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startStream() throws ParseException {
        try {
            handler.startRDF();
        } catch (RDFHandlerException e) {
            throw new ParseException(e);
        }
    }

    @Override
    public void endStream() throws ParseException {
        try {
            handler.endRDF();
        } catch (RDFHandlerException e) {
            throw new ParseException(e);
        }
    }

    @Override
    public boolean setProperty(String key, Object value) {
        if (RDF4JSink.RDF_HANDLER_PROPERTY.equals(key)
                && value instanceof RDFHandler) {
            handler = (RDFHandler) value;
        } else if (RDF4JSink.VALUE_FACTORY_PROPERTY.equals(key)
                && value instanceof ValueFactory) {
            valueFactory = (ValueFactory) value;
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void setBaseUri(String baseUri) {
    }

}
