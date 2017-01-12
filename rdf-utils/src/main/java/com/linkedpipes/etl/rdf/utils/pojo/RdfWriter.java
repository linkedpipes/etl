package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfSource;

public class RdfWriter {

    public interface Writable {

        void write(RdfSource.TripleWriter writer);

    }

}
