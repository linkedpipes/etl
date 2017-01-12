package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EVENTS;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractEventTest {

    private static class DefaultEvent extends AbstractEvent {

        public DefaultEvent(String type) {
            super(type);
        }

        public DefaultEvent(String type, String label) {
            super(type, label);
        }
    }

    @Test
    public void createWithType() {
        final AbstractEvent event = new DefaultEvent("http://type");
        event.setIri("http://event");
        final RdfSource.TripleWriter writer =
                Mockito.mock(RdfSource.TripleWriter.class);
        event.write(writer);
        //
        Mockito.verify(writer).iri("http://event", RDF.TYPE, "http://type");
        Mockito.verify(writer, Mockito.never()).string(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(writer).typed(Mockito.eq("http://event"),
                Mockito.eq(LP_EVENTS.HAS_CREATED), Mockito.anyString(),
                Mockito.eq(XSD.DATETIME));
    }

    @Test
    public void createWithTypeAndLabel() {
        final AbstractEvent event = new DefaultEvent("http://type", "label");
        event.setIri("http://event");
        final RdfSource.TripleWriter writer =
                Mockito.mock(RdfSource.TripleWriter.class);
        event.write(writer);
        //
        Mockito.verify(writer).iri("http://event", RDF.TYPE, "http://type");
        Mockito.verify(writer).string("http://event", SKOS.PREF_LABEL,
                "label", "en");
        Mockito.verify(writer).typed(Mockito.eq("http://event"),
                Mockito.eq(LP_EVENTS.HAS_CREATED), Mockito.anyString(),
                Mockito.eq(XSD.DATETIME));
    }

}
