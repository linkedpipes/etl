package com.linkedpipes.etl.executor.monitor.debug.http;

import com.linkedpipes.etl.executor.monitor.debug.DataUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DataUnitRootEntry extends DebugEntry {

    final DataUnit dataUnit;

    private long totalEntryCount;

    final CreatePublicPath createPublicPath;

    public DataUnitRootEntry(
            DataUnit dataUnit, CreatePublicPath createPublicPath) {
        this.dataUnit = dataUnit;
        this.createPublicPath = createPublicPath;
    }

    @Override
    public DebugEntry prepareData(
            String nameFilter, String sourceFilter, long offset, long limit)
            throws IOException {
        List<ResponseContent.Entry> data =
                collectEntries(nameFilter, sourceFilter, offset, limit);
        //
        ResponseContent content = new ResponseContent(data);
        content.metadata.count = totalEntryCount;
        content.metadata.type = ResponseContent.TYPE_DIR;
        contentAsString = content.asJsonString();
        return this;
    }

    private List<ResponseContent.Entry> collectEntries(
            String nameFilter, String sourceFilter, long offset, long limit) {
        long end = offset + limit;
        List<ResponseContent.Entry> data = new ArrayList<>();
        totalEntryCount = 0;
        for (File directory : dataUnit.getDebugDirectories()) {
            File[] files = directory.listFiles();
            if (files == null) {
                continue;
            }
            // Source filter.
            if (!filterBySource(directory, sourceFilter)) {
                continue;
            }
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
                    // Continue to count number of entries.
                    continue;
                }
                if (file.isDirectory()) {
                    data.add(new ResponseContent.Entry(
                            ResponseContent.TYPE_DIR,
                            file.getName(),
                            directory.getName()));
                } else {
                    data.add(new ResponseContent.Entry(
                            ResponseContent.TYPE_FILE,
                            file.getName(),
                            directory.getName(),
                            file.length(),
                            FileContentEntry.getMimeType(file),
                            createPublicPath.apply(file)));
                }
            }
        }
        return data;
    }

    private boolean filterBySource(File file, String filter) {
        return filter == null || filter.equals(file.getName());
    }

    private boolean filterByName(File file, String filter) {
        return filter == null || file.getName().startsWith(filter);
    }

}
