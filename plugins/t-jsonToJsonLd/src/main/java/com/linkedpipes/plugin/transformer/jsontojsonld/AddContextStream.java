package com.linkedpipes.plugin.transformer.jsontojsonld;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

class AddContextStream extends InputStream {

    public static class Configuration {

        String context;

        String encoding;

        boolean useFileName;

        String fileNamePredicate;

        String dataPredicate;

        String type;
    }

    private final List<InputStream> sources = new ArrayList<>(3);

    private int sourcesIndex;

    public AddContextStream(Configuration configuration,
            String fileName, InputStream stream) throws
            UnsupportedEncodingException {
        final String headerAsString = buildHeader(configuration, fileName);
        final String encoding = configuration.encoding;
        sources.add(new ByteArrayInputStream(
                headerAsString.getBytes(encoding)));
        sources.add(stream);
        sources.add(new ByteArrayInputStream("}\n".getBytes(encoding)));
    }

    private static String buildHeader(Configuration configuration,
            String fileName) {
        StringBuilder header = new StringBuilder();
        header.append("{ \"@context\" : ");
        header.append(configuration.context);
        header.append(",");
        header.append("\"@type\" : \"");
        header.append(configuration.type);
        header.append("\" ,");

        if (configuration.useFileName) {
            header.append("\"");
            header.append(configuration.fileNamePredicate);
            header.append("\" : \"");
            header.append(fileName);
            header.append("\" ,");
        }

        header.append("\"");
        header.append(configuration.dataPredicate);
        header.append("\" : ");

        return header.toString();
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
