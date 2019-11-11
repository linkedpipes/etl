package com.linkedpipes.etl.executor.monitor.debug.http;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * While in all other entries we filter the content only,
 * here we also used filters for disambiguation.
 *
 * <p>If only one entry remains then we return that entry.
 */
class AmbiguousEntry extends DebugEntry {

    List<DebugEntry> entries;

    private long totalEntryCount;

    private long collectedEntries;

    final CreatePublicPath createPublicPath;

    public AmbiguousEntry(
            List<DebugEntry> entries, CreatePublicPath createPublicPath) {
        this.entries = entries;
        this.createPublicPath = createPublicPath;
    }

    @Override
    public DebugEntry prepareData(
            String nameFilter, String sourceFilter, long offset, long limit)
            throws IOException {
        List<DebugEntry> newEntries = filterEntries(sourceFilter);
        if (newEntries.size() == 1) {
            return newEntries.get(0).prepareData(
                    nameFilter, sourceFilter, offset, limit);
        }
        //
        ResponseContent content = prepareResponseContent(
                nameFilter, sourceFilter, offset, limit);
        contentAsString = content.asJsonString();
        return this;
    }

    private List<DebugEntry> filterEntries(String sourceFilter) {
        if (sourceFilter == null) {
            return entries;
        }
        return entries.stream()
                .filter((item) -> filterBySource(item, sourceFilter))
                .collect(Collectors.toList());
    }

    private boolean filterBySource(DebugEntry entry, String filter) {
        if (entry instanceof DirectoryEntry) {
            return filter.equals(((DirectoryEntry) entry).source);
        } else if (entry instanceof FileContentEntry) {
            return filter.equals(((FileContentEntry) entry).source);
        } else {
            return false;
        }
    }

    ResponseContent prepareResponseContent(
            String nameFilter, String sourceFilter, long offset, long limit) {
        List<ResponseContent.Entry> data = collectEntries(
                nameFilter, sourceFilter, offset, limit);
        ResponseContent content = new ResponseContent(data);
        content.metadata.count = totalEntryCount;
        content.metadata.type = ResponseContent.TYPE_AMBIGUOUS;
        return content;
    }

    private List<ResponseContent.Entry> collectEntries(
            String nameFilter, String sourceFilter, long offset, long limit) {
        List<ResponseContent.Entry> data = new ArrayList<>();
        totalEntryCount = 0;
        collectedEntries = 0;
        for (DebugEntry entry : entries) {
            if (entry instanceof DirectoryEntry) {
                data.addAll(collectFromDirectory(
                        (DirectoryEntry) entry,
                        nameFilter, sourceFilter, offset, limit));
            } else if (entry instanceof FileContentEntry) {
                data.addAll(collectFromFile(
                        (FileContentEntry) entry,
                        nameFilter, sourceFilter, offset, limit));

            } else {
                // Ignore entry.
            }
        }
        return data;
    }

    private List<ResponseContent.Entry> collectFromDirectory(
            DirectoryEntry entry,
            String nameFilter, String sourceFilter, long offset, long limit) {
        long remainingOffset = Math.max(0, offset - totalEntryCount);
        long remainingLimit = limit - collectedEntries;
        ResponseContent content = DirectoryEntry.prepareResponse(
                entry.directory, entry.source, nameFilter, sourceFilter,
                remainingOffset, remainingLimit, createPublicPath);
        totalEntryCount += content.metadata.count;
        collectedEntries += content.data.size();
        return content.data;
    }

    private boolean filterByName(File file, String filter) {
        return filter == null || !file.getName().startsWith(filter);
    }

    private List<ResponseContent.Entry> collectFromFile(
            FileContentEntry entry,
            String nameFilter, String sourceFilter, long offset, long limit) {
        totalEntryCount++;
        if (collectedEntries >= limit) {
            // Continue to count number of entries.
            return Collections.emptyList();
        }
        if (sourceFilter != null && !sourceFilter.equals(entry.source)) {
            return Collections.emptyList();
        }
        if (!filterByName(entry.file, nameFilter)) {
            return Collections.emptyList();
        }
        if (totalEntryCount <= offset) {
            return Collections.emptyList();
        }
        //
        collectedEntries++;
        return Arrays.asList(new ResponseContent.Entry(
                ResponseContent.TYPE_FILE,
                entry.file.getName(),
                entry.source,
                entry.getFileSize(),
                entry.getFileMimeType(),
                createPublicPath.apply(entry.file)));

    }

}
