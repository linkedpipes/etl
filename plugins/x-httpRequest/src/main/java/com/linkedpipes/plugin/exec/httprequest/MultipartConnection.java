package com.linkedpipes.plugin.exec.httprequest;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;

class MultipartConnection extends Connection {

    public static final String EOL = "\r\n";

    private final String boundary;

    private final OutputStream outputStream;

    private final PrintWriter writer;

    public MultipartConnection(HttpURLConnection connection)
            throws IOException {
        super(connection);
        this.boundary = "----------------------" + System.currentTimeMillis();

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

    public void addField(String name, String value) {
        writeBoundary();
        writeFieldHeader(name);
        writer.flush();

        writer.append(value).append(EOL);
        writer.flush();
    }

    private void writeBoundary() {
        writer.append("--" + boundary).append(EOL);
    }

    private void writeFieldHeader(String name) {
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"");
        writer.append(EOL);
        writer.append("Content-Type: text/plain; charset=UTF-8");
        writer.append(EOL);
        writer.append(EOL);
    }

    public void addFile(String name, String fileName, File uploadFile)
            throws IOException {
        writeBoundary();
        writeFileHeader(name, fileName);
        writer.flush();

        FileUtils.copyFile(uploadFile, outputStream);
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
    public void finishRequest() throws IOException {
        writeEndOfRequest();
        writer.close();
    }

    private void writeEndOfRequest() {
        writer.append(EOL);
        writer.append("--" + boundary + "--").append(EOL);
        writer.flush();
    }

}
