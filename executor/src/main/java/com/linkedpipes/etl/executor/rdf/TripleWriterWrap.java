package com.linkedpipes.etl.executor.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.RdfFormatter;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;

import java.util.Date;

public class TripleWriterWrap implements TripleWriter {

    private final BackendTripleWriter writer;

    private final RdfFormatter format = new RdfFormatter();

    public TripleWriterWrap(BackendTripleWriter writer) {
        this.writer = writer;
    }

    @Override
    public void iri(String subject, String predicate, String object) {
        writer.iri(subject, predicate, object);
    }

    @Override
    public void string(String subject, String predicate, String object) {
        writer.string(subject, predicate, object, null);
    }

    @Override
    public void string(
            String subject, String predicate, String object, String lang) {
        writer.string(subject, predicate, object, lang);
    }

    @Override
    public void date(String subject, String predicate, Date object) {
        String value = format.toXsdDate(object);
        typed(subject, predicate, value, XSD.DATETIME);
    }

    @Override
    public void typed(
            String subject, String predicate, String object, String type) {
        writer.typed(subject, predicate, object, type);
    }

    @Override
    public void flush() throws RdfException {
        try {
            writer.flush();
        } catch (RdfUtilsException ex) {
            throw new RdfException("", ex);
        }
    }

}
