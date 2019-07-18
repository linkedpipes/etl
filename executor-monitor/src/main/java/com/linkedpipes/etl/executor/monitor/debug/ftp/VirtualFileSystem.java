package com.linkedpipes.etl.executor.monitor.debug.ftp;

import com.linkedpipes.etl.executor.monitor.debug.DataUnit;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Virtual file system.
 * Follow structure: {execution}/debug/{data unit}
 */
@Service
public class VirtualFileSystem {

    private static final Logger LOG =
            LoggerFactory.getLogger(VirtualFileSystem.class);

    /**
     * Represent a home path.
     */
    static final Path ROOT_PATH = new Path("/", true, null, null, null);

    /**
     * Represent path in the virtual file system.
     */
    static class Path {

        private final String ftpPath;

        private final boolean synthetic;

        private final File path;

        private final DebugData execution;

        private final DataUnit dataUnit;

        private Path(String ftpPath, boolean synthetic, File path,
                     DebugData execution, DataUnit dataUnit) {
            this.ftpPath = ftpPath;
            this.synthetic = synthetic;
            this.path = path;
            this.execution = execution;
            this.dataUnit = dataUnit;
        }

        /**
         * True if represents a synthetic path.
         */
        boolean isSynthetic() {
            return synthetic;
        }

        /**
         * Return FTP representation of the path.
         */
        String getFtpPath() {
            return ftpPath;
        }

        /**
         * If {@link #isSynthetic()} is true then returned value is null.
         *
         * @return Null if this path does not represent any existing file.
         */
        File getFile() {
            return path;
        }

    }

    private class RootDirectory extends AbstractFtpDirectory {

        private final ExecutionFacade executions;

        RootDirectory(ExecutionFacade executions, String ftpPath) {
            super(ftpPath);
            this.executions = executions;
        }

        @Override
        public List<FtpFile> listFiles() {
            final List<FtpFile> result = new ArrayList<>();
            for (Execution execution : executions.getExecutions()) {
                if (execution.getDebugData() != null) {
                    result.add(new ExecutionDirectory(execution.getDebugData(),
                            ftpPath + "/" + execution.getId()));
                }
            }
            return result;
        }

        @Override
        public Object getPhysicalFile() {
            return null;
        }
    }

    private class ExecutionDirectory extends AbstractFtpDirectory {

        final DebugData execution;

        ExecutionDirectory(DebugData debugData, String ftpPath) {
            super(ftpPath);
            this.execution = debugData;
        }

        @Override
        public List<FtpFile> listFiles() {
            List<FtpFile> result = new ArrayList<>();
            for (DataUnit dataUnit
                    : execution.getDataUnits().values()) {
                result.add(new DataUnitDirectory(execution, dataUnit,
                        ftpPath + "/" + dataUnit.getName()));
            }
            return result;
        }

        @Override
        public Object getPhysicalFile() {
            return null;
        }

    }

    private class DataUnitDirectory extends AbstractFtpDirectory {

        private final DataUnit dataUnit;

        DataUnitDirectory(DebugData debugData, DataUnit dataUnit,
                          String ftpPath) {
            super(ftpPath);
            this.dataUnit = dataUnit;
            // We need the execution directory, but if we use data
            // from this execution we can not get it from
            // {@link #executions} as it has not been yet loaded.
            if (debugData.getExecutionId().equals(dataUnit.getExecutionId())) {
                // We use data from this execution.
                dataUnit.updateDebugDirectories(
                        debugData.getExecutionDirectory());
            } else {
                final Execution execution = executions.getExecution(
                        dataUnit.getExecutionId());
                if (execution == null) {
                    LOG.warn("Missing referenced execution: {}",
                            dataUnit.getExecutionId());
                } else {
                    // Load directories.
                    dataUnit.updateDebugDirectories(execution.getDirectory());
                }

            }
        }

        @Override
        public List<FtpFile> listFiles() {
            // List content of debug directories, without the debug directories.
            List<FtpFile> result = new ArrayList<>();
            for (File directory : dataUnit.getDebugDirectories()) {
                File[] files = directory.listFiles();
                if (files == null) {
                    continue;
                }
                for (File file : files) {
                    result.add(new ReadonlyFtpFile(
                            ftpPath + "/" + file.getName(), file));
                }
            }
            return result;
        }

        @Override
        public Object getPhysicalFile() {
            return null;
        }

    }

    public VirtualFileSystem() {
    }

    /**
     * Return ciew of the virtual FTP file system.
     */
    public FileSystemView getView() {
        return new VirtualFileSystemView(this);
    }

    @Autowired
    private ExecutionFacade executions;

    /**
     * Return Null in case of an invalid path.
     */
    Path resolvePath(String path) {
        LinkedList<String> parsedPath = parsePath(path);
        String ftpPath = joinPath(parsedPath);
        if (parsedPath.isEmpty()) {
            return ROOT_PATH;
        }
        // Search for an execution.
        Execution execution = executions.getExecution(parsedPath.removeFirst());
        if (execution == null || execution.getDebugData() == null) {
            return null;
        }
        if (parsedPath.isEmpty()) {
            return new Path(ftpPath, true, null, execution.getDebugData(),
                    null);
        }
        // Search for data unit.
        DataUnit dataUnit = execution.getDebugData().getDataUnits().get(
                parsedPath.removeFirst());
        if (dataUnit == null) {
            return null;
        }
        if (parsedPath.isEmpty()) {
            return new Path(ftpPath, true, null, execution.getDebugData(),
                    dataUnit);
        }
        // The execution can be change here.
        if (dataUnit.isMapped()) {
            execution = executions.getExecution(dataUnit.getExecutionId());
            if (execution == null) {
                // Missing execution.
                return null;
            }
        }
        // Make sure the content of the data unit is loaded.
        dataUnit.updateDebugDirectories(execution.getDirectory());
        // Search for a directory in the data unit. The file
        // can be in any of the debugging directories.
        for (File directory : dataUnit.getDebugDirectories()) {
            File file = new File(directory, joinPath(parsedPath));
            if (file.exists()) {
                return new Path(ftpPath, false, file, null, null);
            }
        }
        return null;
    }

    /**
     * Return Null in case of an invalid path.
     */
    Path resolvePath(String base, String path) {
        if (path.startsWith("/")) {
            // Path from the root element.
            return resolvePath(path);
        } else {
            return resolvePath(base + '/' + path);
        }
    }

    FtpFile getFile(Path path) {
        if (path.isSynthetic()) {
            if (path.execution == null) {
                return new RootDirectory(executions, path.ftpPath);
            } else if (path.dataUnit == null) {
                return new ExecutionDirectory(path.execution, path.ftpPath);
            } else {
                return new DataUnitDirectory(path.execution, path.dataUnit,
                        path.ftpPath);
            }
        } else {
            return new ReadonlyFtpFile(path.ftpPath, path.path);
        }
    }

    /**
     * Parse FTP path into parsed path.
     */
    private static LinkedList<String> parsePath(String path) {
        String fullPath = path.replace("\\", "/");
        // Replace ., ~ and ..
        StringTokenizer tokenizer = new StringTokenizer(fullPath, "/");
        LinkedList<String> result = new LinkedList<>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (".".equals(token)) {
                continue;
            }
            if ("..".equals(token)) {
                // Remove record from result. ie simulate directory up.
                result.removeLast();
                continue;
            }
            result.addLast(token);
        }
        return result;
    }

    /**
     * From parsed path construct FTP path.
     */
    private static String joinPath(List<String> parserPath) {
        if (parserPath.isEmpty()) {
            return ROOT_PATH.getFtpPath();
        }
        // Construct path.
        StringBuilder path = new StringBuilder();
        parserPath.forEach((item) -> {
            path.append("/");
            path.append(item);
        });
        return path.toString();
    }

}
