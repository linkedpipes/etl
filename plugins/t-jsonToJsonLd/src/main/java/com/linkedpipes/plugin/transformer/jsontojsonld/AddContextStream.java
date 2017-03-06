package com.linkedpipes.plugin.transformer.jsontojsonld;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

class AddContextStream extends InputStream  {

    private final List<InputStream> sources = new ArrayList<>(3);

    private int sourcesIndex;

    public AddContextStream(String vocabulary,
            String encoding,
            InputStream stream,
            String fileName) throws
            UnsupportedEncodingException {
        final String headerAsString = String.format(""
                + "{\n"
                + "  \"@context\": {\n"
                + "    \"@vocab\": \"%s\""
                + "  },\n"
                + "  \"@type\": \"http://localhost/ontology/temp/DataRoot\",\n"
                + "  \"fileName\": \"" + fileName + "\",\n"
                + "  \"data\":"
                + "", vocabulary);
        sources.add(new ByteArrayInputStream(headerAsString.getBytes(encoding)));
        sources.add(stream);
        sources.add(new ByteArrayInputStream("}\n".getBytes(encoding)));
    }

    @Override
    public int read() throws IOException {
        if (sourcesIndex < sources.size()) {
            int output = sources.get(sourcesIndex).read();
            if (output == -1) {
                sourcesIndex++;
                return read();
            } else {
                return output;
            }
        } else {
            // All input streams have been read.
            return -1;
        }
    }

    @Override
    public void close() throws IOException {
        for (InputStream source : sources) {
            source.close();
        }
    }

}
