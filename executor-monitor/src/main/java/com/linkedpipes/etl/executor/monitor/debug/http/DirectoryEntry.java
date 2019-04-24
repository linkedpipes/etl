package com.linkedpipes.etl.executor.monitor.debug.http;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DirectoryEntry extends DebugEntry {

    final File directory;

    final String source;

    public DirectoryEntry(File directory, String source) {
        this.directory = directory;
        this.source = source;
    }

    @Override
    public DebugEntry prepareData(
            String nameFilter, String sourceFilter, long offset, long limit)
            throws IOException {
        ResponseContent content = prepareResponse(
                directory, source, nameFilter, sourceFilter, offset, limit);
        contentAsString = content.asJsonString();
        return this;
    }

    public static ResponseContent prepareResponse(
            File directory, String source,
            String nameFilter, String sourceFilter, long offset, long limit) {
        long totalEntryCount = 0;
        long end = offset + limit;
        File[] files = directory.listFiles();
        if (files == null) {
            return createContentResponse(
                    Collections.emptyList(), totalEntryCount);
        }
        // Source filter.
        if (sourceFilter != null && !sourceFilter.equals(source)) {
            return createContentResponse(
                    Collections.emptyList(), totalEntryCount);
        }
        // Search for files.
        List<ResponseContent.Entry> data = new ArrayList<>();
        for (File file : files) {
            // Name filter.
            if (!filterByName(file, nameFilter)) {
                continue;
            }
            totalEntryCount++;
            if (totalEntryCount <= offset) {
                continue;
            }
            if (totalEntryCount > end) {
                // Continue counting files.
                continue;
            }
            if (file.isDirectory()) {
                data.add(new ResponseContent.Entry(
                        ResponseContent.TYPE_DIR,
                        file.getName(),
                        source));
            } else {
                data.add(new ResponseContent.Entry(
                        ResponseContent.TYPE_FILE,
                        file.getName(),
                        source,
                        file.length(),
                        FileContentEntry.getMimeType(file)));
            }
        }

        return createContentResponse(data, totalEntryCount);
    }

    private static ResponseContent createContentResponse(
            List<ResponseContent.Entry> data, long count) {
        ResponseContent content = new ResponseContent(data);
        content.metadata.count = count;
        content.metadata.type = ResponseContent.TYPE_DIR;
        return content;
    }

    private static boolean filterByName(File file, String filter) {
        return filter == null || file.getName().startsWith(filter);
    }

}
