package com.linkedpipes.etl.executor.monitor.web.servlet;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class GetExecutionLogTailHandler {

    private static final Logger LOG =
            LoggerFactory.getLogger(GetExecutionLogTailHandler.class);

    public void handle(
            HttpServletResponse response, File logFile, int count)
            throws IOException {
        String[] lines = this.readLogTail(logFile, count);
        this.writeLinesToResponse(lines, response);
    }

    private String[] readLogTail(File file, int count) throws IOException {
        ReversedLinesFileReader reader = new ReversedLinesFileReader(file);
        String[] lines = new String[count];
        for (int i = count - 1; i >= 0; --i) {
            String line;
            try {
                line = reader.readLine();
            } catch (Exception ex) {
                LOG.error("Can't read log file, i: {}", i, ex);
                break;
            }
            if (line == null) {
                break;
            } else {
                lines[i] = line;
            }
        }
        reader.close();
        return lines;
    }

    private void writeLinesToResponse(
            String[] lines, HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = response.getWriter();
        for (int i = 0; i < lines.length; ++i) {
            if (lines[i] != null) {
                writer.write(lines[i]);
                writer.write("\n");
            }
        }
        writer.flush();
    }

}
