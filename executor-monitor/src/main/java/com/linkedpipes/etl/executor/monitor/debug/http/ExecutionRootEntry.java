package com.linkedpipes.etl.executor.monitor.debug.http;

import com.linkedpipes.etl.executor.monitor.debug.DataUnit;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ExecutionRootEntry extends DebugEntry {

    final DebugData debugData;

    public ExecutionRootEntry(DebugData debugData) {
        this.debugData = debugData;
    }

    @Override
    public DebugEntry prepareData(
            String nameFilter, String sourceFilter, long offset, long limit)
            throws IOException {
        List<ResponseContent.Entry> data = debugData.getDataUnits()
                .entrySet()
                .stream()
                .filter((item) -> filterByName(item, nameFilter))
                .filter((item) -> filterBySource(item, sourceFilter))
                .map((item) -> createEntry(item))
                .sorted(Comparator.comparing(item -> item.name))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
        ResponseContent content = new ResponseContent(data);
        content.metadata.count = (long)debugData.getDataUnits().size();
        content.metadata.type = ResponseContent.TYPE_DIR;
        contentAsJsonString = content.asJsonString();
        return this;
    }

    private ResponseContent.Entry createEntry(
            Map.Entry<String, DataUnit> entry) {
        return new ResponseContent.Entry(
                ResponseContent.TYPE_DIR,
                entry.getKey(),
                entry.getKey(),
                null);
    }

    private static boolean filterByName(
            Map.Entry<String, DataUnit> entry, String nameFilter) {
        if (nameFilter == null) {
            return true;
        }
        return entry.getValue().getName().startsWith(nameFilter);
    }

    private static boolean filterBySource(
            Map.Entry<String, DataUnit> entry, String sourceFilter) {
        // Source is always null.
        return sourceFilter == null;
    }

}
