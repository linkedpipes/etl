package com.linkedpipes.etl.rdf.utils;

import org.junit.Test;
import org.mockito.Mockito;

public class RdfBuilderTest {

    @Test
    public void simple() throws RdfUtilsException {
        final RdfSource.TripleWriter writer =
                Mockito.mock(RdfSource.TripleWriter.class);
        final RdfSource source = Mockito.mock(RdfSource.class);
        Mockito.when(source.getTripleWriter(Mockito.any())).thenReturn(writer);
        //
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        builder.entity("http://a")
                .iri("http://value", "a")
                .entity("http://link", "http://b")
                .string("http://value", "b");
        builder.commit();
        //
        Mockito.verify(writer).iri("http://a", "http://value", "a");
        Mockito.verify(writer).iri("http://a", "http://link", "http://b");
        Mockito.verify(writer).string("http://b", "http://value", "b", null);
    }

}
