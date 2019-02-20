package com.linkedpipes.etl.executor.monitor.debug.http;


import com.linkedpipes.etl.executor.monitor.debug.DataUnit;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.executor.monitor.debug.DebugDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class HttpDebugFilesFacade {

    private final DebugDataSource dataSource;

    @Autowired
    public HttpDebugFilesFacade(DebugDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<DebugEntry> resolve(String pathAsString) {
        if (pathAsString.contains("..")) {
            // We do not support navigation.
            return Optional.empty();
        }
        String[] path = pathAsString.split("/");
        DebugData debugData = dataSource.getDebugData(path[0]);
        if (debugData == null) {
            return Optional.empty();
        }
        return resolveDebugData(Arrays.asList(path), debugData);
    }

    private Optional<DebugEntry> resolveDebugData(
            List<String> path, DebugData debugData) {
        if (path.size() == 1) {
            return Optional.of(new ExecutionRootEntry(debugData));
        }
        String dataUnitName = path.get(1);
        DataUnit dataUnit = debugData.getDataUnits().get(dataUnitName);
        if (dataUnit == null) {
            return Optional.empty();
        }
        dataUnit.updateDebugDirectories(debugData.getExecutionDirectory());
        return resolveDataUnit(path, dataUnit);
    }

    private Optional<DebugEntry> resolveDataUnit(
            List<String> path, DataUnit dataUnit) {
        if (path.size() == 2) {
            return Optional.of(new DataUnitRootEntry(dataUnit));
        }
        // The same file can be in multiple data units.
        List<DebugEntry> entriesFound = new ArrayList<>(2);
        for (File file : dataUnit.getDebugDirectories()) {
            File resolved = resolvePath(path, 2, file);
            if (resolved == null) {
                continue;
            }
            if (resolved.isDirectory()) {
                entriesFound.add(new DirectoryEntry(resolved, file.getName()));
            } else {
                entriesFound.add(new FileContentEntry(
                        dataUnit, resolved, file.getName()));
            }
        }
        if (entriesFound.size() == 0) {
            return Optional.empty();
        } else if (entriesFound.size() == 1) {
            return Optional.of(entriesFound.get(0));
        } else {
            return Optional.of(new AmbiguousEntry(entriesFound));
        }
    }

    private File resolvePath(
            List<String> path, int pathIndex, File currentFile) {
        if (path.size() == pathIndex) {
            return currentFile;
        }
        File nextFile = new File(currentFile, path.get(pathIndex));
        if (nextFile.exists() || nextFile.isDirectory()) {
            return resolvePath(path, pathIndex + 1, nextFile);
        } else {
            return null;
        }
    }

}
