package com.linkedpipes.etl.executor.monitor.debug.ftp;

import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Virtual file system with following structure:
 * {execution}/debug/{data unit}
 *
 * @author Petr Škoda
 */
@Service
public class VirtualFileSystem {

    private static final Logger LOG = LoggerFactory.getLogger(VirtualFileSystem.class);

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

        private final DebugData.DataUnit dataUnit;

        private Path(String ftpPath, boolean synthetic, File path,
                DebugData execution, DebugData.DataUnit dataUnit) {
            this.ftpPath = ftpPath;
            this.synthetic = synthetic;
            this.path = path;
            this.execution = execution;
            this.dataUnit = dataUnit;
        }

        /**
         *
         * @return True if represents a synthetic path.
         */
        boolean isSynthetic() {
            return synthetic;
        }

        /**
         *
         * @return FTP representation of the path.
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

    private static class RootDirectory extends AbstractFtpDirectory {

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

    }

    private static class ExecutionDirectory extends AbstractFtpDirectory {

        final DebugData execution;

        ExecutionDirectory(DebugData execution, String ftpPath) {
            super(ftpPath);
            this.execution = execution;
        }

        @Override
        public List<FtpFile> listFiles() {
            final List<FtpFile> result = new ArrayList<>();
            for (DebugData.DataUnit dataUnit
                    : execution.getDataUnits().values()) {
                result.add(new DataUnitDirectory(execution, dataUnit,
                        ftpPath + "/" + dataUnit.getId()));
            }
            return result;
        }

    }

    private static class DataUnitDirectory extends AbstractFtpDirectory {

        private final DebugData execution;

        private final DebugData.DataUnit dataUnit;

        DataUnitDirectory(DebugData execution, DebugData.DataUnit dataUnit,
                String ftpPath) {
            super(ftpPath);
            this.execution = execution;
            this.dataUnit = dataUnit;
        }

        @Override
        public List<FtpFile> listFiles() {
            // List content of debug directories, without the debug directories.
            final List<FtpFile> result = new ArrayList<>();
            for (String directory : dataUnit.getDebugDirectories()) {
                final File debugDirectory = new File(execution.getDirectory(),
                        directory);
                for (File file : debugDirectory.listFiles()) {
                    result.add(new ReadonlyFtpFile(
                            ftpPath + "/" + file.getName(), file));
                }
            }
            return result;
        }

    }

    public VirtualFileSystem() {
    }

    /**
     *
     * @return View of the virtual FTP file system.
     */
    public FileSystemView getView() {
        return new VirtualFileSystemView(this);
    }

    @Autowired
    private ExecutionFacade executions;

    /**
     *
     * @param path
     * @return Null in case of an invalid path.
     */
    Path resolvePath(String path) {
        final LinkedList<String> parsedPath = parsePath(path);
        final String ftpPath = joinPath(parsedPath);
        if (parsedPath.isEmpty()) {
            return ROOT_PATH;
        }
        // Search for an execution.
        final Execution execution = executions.getExecution(
                parsedPath.removeFirst());
        if (execution == null || execution.getDebugData() == null) {
            return null;
        }
        if (parsedPath.isEmpty()) {
            return new Path(ftpPath, true, null, execution.getDebugData(),
                    null);
        }
        // Search for data unit.
        final DebugData.DataUnit dataUnit
                = execution.getDebugData().getDataUnits().get(
                        parsedPath.removeFirst());
        if (dataUnit == null) {
            return null;
        }
        if (parsedPath.isEmpty()) {
            return new Path(ftpPath, true, null, execution.getDebugData(),
                    dataUnit);
        }
        // Search for a directory in the data unit. The file
        // can be in any of the debugging directories.
        for (String directory : dataUnit.getDebugDirectories()) {
            final String filePath = execution.getDirectory() + "/"
                    + directory + "/" + joinPath(parsedPath);
            final File file = new File(filePath);
            if (file.exists()) {
                return new Path(ftpPath, false, file, null, null);
            }
        }
        return null;
    }

    /**
     *
     * @param base
     * @param path
     * @return Null in case of an invalid path.
     */
    Path resolvePath(String base, String path) {
        return resolvePath(base + '/' + path);
    }

    FtpFile getFile(Path path) {
        LOG.info("getFile: {}", path.ftpPath);
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
     *
     * @param path
     * @return
     */
    private static LinkedList<String> parsePath(String path) {
        final String fullPath = path.replace("\\", "/");
        // Replace ., ~ and ..
        final StringTokenizer tokenizer = new StringTokenizer(fullPath, "/");
        final LinkedList<String> result = new LinkedList<>();
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
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
     *
     * @param parserPath
     * @return
     */
    private static String joinPath(List<String> parserPath) {
        if (parserPath.isEmpty()) {
            return ROOT_PATH.getFtpPath();
        }
        // Construct path.
        final StringBuilder path = new StringBuilder();
        parserPath.forEach((item) -> {
            path.append("/");
            path.append(item);
        });
        return path.toString();
    }

}
