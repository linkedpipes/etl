package com.linkedpipes.plugin.xpipeline;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

class MultipartConnection extends Connection {

    public static final String EOL = "\r\n";

    private final String boundary;

    private final OutputStream outputStream;

    private final PrintWriter writer;

    public MultipartConnection(HttpURLConnection connection)
            throws IOException {
        super(connection);
        this.boundary = "=----------------------" + System.currentTimeMillis();

        initializeConnection();

        outputStream = connection.getOutputStream();
        writer = new PrintWriter(
                new OutputStreamWriter(outputStream, "UTF-8"),
                true);
    }

    private void initializeConnection() {
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);
        // Use chunk mode with auto chunk size. This is necessary for
        // large data, otherwise HttpURLConnection tries to store all
        // the data to calculate length (for header).
        connection.setChunkedStreamingMode(0);
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
    }

    private void writeBoundary() {
        writer.append("--" + boundary).append(EOL);
    }

    public void addStream(
            String name, String fileName, Consumer<OutputStream> content) {
        writeBoundary();
        writeFileHeader(name, fileName);
        writer.flush();
        content.accept(outputStream);
        writer.append(EOL);
        writer.flush();
    }

    private void writeFileHeader(String name, String fileName) {
        writer.append("Content-Disposition: form-data; name=\"" + name
                + "\"; filename=\"" + fileName + "\"");
        writer.append(EOL);
        writer.append("Content-Type: application/octet-stream");
        writer.append(EOL);
        writer.append("Content-Transfer-Encoding: binary");
        writer.append(EOL);
        writer.append(EOL);
    }

    @Override
    public void finishRequest() {
        writeEndOfRequest();
        writer.close();
    }

    private void writeEndOfRequest() {
        writer.append(EOL);
        writer.append("--" + boundary + "--").append(EOL);
        writer.flush();
    }

}
