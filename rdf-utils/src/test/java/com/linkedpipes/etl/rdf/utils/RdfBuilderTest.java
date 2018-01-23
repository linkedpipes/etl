package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import org.junit.Test;
import org.mockito.Mockito;

public class RdfBuilderTest {

    @Test
    public void simple() throws RdfUtilsException {
        BackendTripleWriter writer = Mockito.mock(BackendTripleWriter.class);
        BackendRdfSource source = Mockito.mock(BackendRdfSource.class);
        Mockito.when(source.getTripleWriter(Mockito.any())).thenReturn(writer);

        RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        builder.entity("http://a")
                .iri("http://value", "a")
                .entity("http://link", "http://b")
                .string("http://value", "b");
        builder.commit();

        Mockito.verify(writer).iri("http://a", "http://value", "a");
        Mockito.verify(writer).iri("http://a", "http://link", "http://b");
        Mockito.verify(writer).string("http://b", "http://value", "b", null);
        Mockito.verify(writer, Mockito.times(1)).flush();
    }

}
