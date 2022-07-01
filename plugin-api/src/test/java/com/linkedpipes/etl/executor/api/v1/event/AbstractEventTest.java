package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP;
import com.linkedpipes.etl.executor.api.v1.vocabulary.RDF;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AbstractEventTest {

    private class DefaultEvent extends AbstractEvent {

        public DefaultEvent(String type) {
            super(type);
        }

    }

    @Test
    public void createNoLanguage() {
        Event event = new DefaultEvent("http://type");
        event.setIri("http://event");
        TripleWriter writer = Mockito.mock(TripleWriter.class);
        event.write(writer);

        Mockito.verify(writer, Mockito.times(1)).iri(
                Mockito.eq("http://event"),
                Mockito.eq(RDF.TYPE),
                Mockito.eq("http://type"));
        Mockito.verify(writer, Mockito.times(1)).date(
                Mockito.eq("http://event"),
                Mockito.eq(LP.HAS_CREATED),
                Mockito.any());
    }

}
