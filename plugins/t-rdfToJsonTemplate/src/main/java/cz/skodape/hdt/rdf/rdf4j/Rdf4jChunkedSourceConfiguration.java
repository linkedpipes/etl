package cz.skodape.hdt.rdf.rdf4j;

import cz.skodape.hdt.core.PropertySource;
import cz.skodape.hdt.model.SourceConfiguration;

import java.io.File;

public class Rdf4jChunkedSourceConfiguration implements SourceConfiguration {

    public File file = null;

    @Override
    public PropertySource createSource() {
        return new Rdf4jChunkedSource(this);
    }


}
