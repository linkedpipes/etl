package com.linkedpipes.executor.monitor.browser.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.linkedpipes.commons.entities.executor.DebugStructure;
import com.linkedpipes.executor.monitor.Configuration;
import com.linkedpipes.executor.monitor.execution.boundary.ExecutionFacade;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.annotation.PostConstruct;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Virtual file system represent a predefined path structure: {execution}/dataunits/{data unit}
 *
 * @author Petr Škoda
 */
@Service
public class VirtualFileSystem {

    public static class ResolvedPath {

        /**
         * Path as visible to the ftp user.
         */
        protected String ftpPath;

        /**
         * Path as it should be used by the ftp server. It represent a synthetic path or path to the real directory. If
         * this variable is null then the given ftpPath is invalid.
         */
        protected String path;

        /**
         * True if real path represents a synthetic directory.
         */
        protected boolean synthetic;

        /**
         * In case of synthetic non root directory store execution.
         */
        protected ExecutionDirectory executionDirectory;

        /**
         * Refer to the data unit directory.
         */
        protected DataUnitDirectory dataUnitDirectory;

        protected ResolvedPath() {

        }

        public String getFtpPath() {
            return ftpPath;
        }

        public String getPath() {
            return path;
        }

        public boolean isSynthetic() {
            return synthetic;
        }

        protected static ResolvedPath crerateRoot() {
            final ResolvedPath rootPath = new ResolvedPath();
            rootPath.ftpPath = HOME_DIRECTORY;
            rootPath.path = null;
            rootPath.synthetic = true;
            rootPath.executionDirectory = null;
            return rootPath;
        }

        protected static ResolvedPath crerateExecutionDirectory(String ftpPath, ExecutionDirectory executionDirectory) {
            final ResolvedPath rootPath = new ResolvedPath();
            rootPath.ftpPath = ftpPath;
            rootPath.path = null;
            rootPath.synthetic = true;
            rootPath.executionDirectory = executionDirectory;
            return rootPath;
        }

        protected static ResolvedPath crerateDataUnitDirectory(String ftpPath, DataUnitDirectory dataUnitDirectory) {
            final ResolvedPath rootPath = new ResolvedPath();
            rootPath.ftpPath = ftpPath;
            rootPath.path = null;
            rootPath.synthetic = true;
            rootPath.executionDirectory = null;
            rootPath.dataUnitDirectory = dataUnitDirectory;
            return rootPath;
        }

        protected static ResolvedPath createNativeDirectory(String ftpPath, String path) {
            final ResolvedPath rootPath = new ResolvedPath();
            rootPath.ftpPath = ftpPath;
            rootPath.path = path;
            rootPath.synthetic = false;
            rootPath.executionDirectory = null;
            return rootPath;
        }

    }

    /**
     * Represents an execution directory.
     */
    public static class ExecutionDirectory {

        /**
         * Map of data units. If value is null then record for given data unit has not yet been loaded.
         */
        protected Map<String, DataUnitDirectory> dataUnits = new HashMap<>();

        /**
         * Debug data for this execution, they are used to construct information about data units.
         */
        protected DebugStructure debugData;

    }

    /**
     * Represents a data unit inside a directory.
     */
    public static class DataUnitDirectory {

        /**
         * Full path to data directories.
         */
        protected List<String> directories = new ArrayList<>(2);

    }

    private static final Logger LOG = LoggerFactory.getLogger(VirtualFileSystem.class);

    public static final String HOME_DIRECTORY = "/";

    /**
     * Path to the physical root directory.
     */
    protected String rootPath;

    /**
     * Root directory.
     */
    protected File root;

    protected final ObjectMapper json = new ObjectMapper();

    /**
     * List of root executions. Do not use directly. Functions to access are
     * {@link #getExecutionDirectory(java.lang.String)} and
     * {@link #getDataUnitDirectory(com.linkedpipes.executor.monitor.browser.entity.VirtualFileSystem.ExecutionDirectory, java.lang.String)}.
     *
     * If value is null, the record for given directory has not been initialized yet.
     */
    protected Map<String, ExecutionDirectory> executions = new HashMap<>();

    @Autowired
    private ExecutionFacade executionFacade;

    @Autowired
    private Configuration configuration;

    public VirtualFileSystem() {
        // No operation here.
    }

    @PostConstruct
    public void init() {
        this.root = configuration.getWorkingDirectory();
        this.rootPath = root.getPath().replace(File.separatorChar, '/').replace('\\', '/');
    }

    /**
     *
     * @param currentDirectory
     * @param ftpPath
     * @return Null if given path does not exists.
     */
    public ResolvedPath resolvePath(String currentDirectory, String ftpPath) {
        final List<String> pathList = constructPath(currentDirectory, ftpPath);
        final String path = listAsPath(pathList);
        //
        if (pathList.isEmpty()) {
            // Root directory.
            return ResolvedPath.crerateRoot();
        }
        // Now we need to create directory based on the path.
        final ExecutionDirectory execution = getExecutionDirectory(pathList.get(0));
        if (execution == null) {
            return null;
        }
        if (pathList.size() == 1) {
            // Synthetic directory for an execution.
            return ResolvedPath.crerateExecutionDirectory(path, execution);
        }
        // Get data unit.
        final DataUnitDirectory dataUnit = getDataUnitDirectory(execution, pathList.get(1));
        if (dataUnit == null) {
            return null;
        }
        if (pathList.size() == 2) {
            return ResolvedPath.crerateDataUnitDirectory(path, dataUnit);
        }
        // It represents a real directory - we need to update path based on a directory.
        // From physical paht we remove the name of execution and replace it with the path to the exection.
        pathList.remove(0);
        pathList.remove(0);
        // For a file we also need to determine the directory the files comes from.
        String realDirectory = null;
        for (String directoryName : dataUnit.directories) {
            final File file = new File(directoryName + "/" + pathList.get(0));
            if (file.exists()) {
                realDirectory = directoryName;
                break;
            }
        }
        if (realDirectory == null) {
            // Direcory does not exists.
            return null;
        }
        //
        final String physicalPath = realDirectory + "/" + listAsPath(pathList);
        return ResolvedPath.createNativeDirectory(path, physicalPath);
    }

    /**
     *
     * @param path Value of {@link ResolvedPath#path}
     * @return
     */
    public AbstractFtpDirectory getSyntheticDirectory(ResolvedPath path) {
        if (path.executionDirectory != null) {
            return new ExecutionFtpDirectory(path.ftpPath, this, path.executionDirectory);
        } else if (path.dataUnitDirectory != null) {
            return new DataUnitFtpDirectory(path.ftpPath, this, path.dataUnitDirectory);
        } else {
            // If nothing set it muset be a root directory.
            return new RootFtpDirectory(path.ftpPath, this);
        }
    }

    public List<FtpFile> getDirectoryContent(RootFtpDirectory directory) {
        // Update content.
        updateExecutionsList();
        // List execution directories.
        final List<FtpFile> result = new ArrayList<>();
        for (Map.Entry<String, ExecutionDirectory> entry : executions.entrySet()) {
            result.add(new ExecutionFtpDirectory(HOME_DIRECTORY + entry.getKey(), this, entry.getValue()));
        }
        return result;
    }

    public List<FtpFile> getDirectoryContent(ExecutionFtpDirectory directory) {
        final List<FtpFile> result = new ArrayList<>();
        // List data unit directories.
        for (Map.Entry<String, DataUnitDirectory> entry : directory.getExecutionDirectory().dataUnits.entrySet()) {
            result.add(new DataUnitFtpDirectory(directory.getAbsolutePath() + "/" + entry.getKey(), this,
                    entry.getValue()));
        }
        return result;
    }

    public List<FtpFile> getDirectoryContent(DataUnitFtpDirectory directory) {
        final List<FtpFile> result = new ArrayList<>();
        // List native directories.
        final DataUnitDirectory dataUnitDirectory = directory.getDataUnitDirectory();
        for (String entry : dataUnitDirectory.directories) {
            final File entryDirectory = new File(entry);
            for (File file : entryDirectory.listFiles()) {
                result.add(new ReadonlyFtpFile(directory.getAbsolutePath() + "/" + file.getName(), file));
            }
        }
        return result;
    }

    /**
     *
     * @param name
     * @return Loaded record about execution directory.
     */
    protected ExecutionDirectory getExecutionDirectory(String name) {
        if (!executions.containsKey(name)) {
            // Load current list of directories.
            updateExecutionsList();
            // Check again.
            if (!executions.containsKey(name)) {
                return null;
            }
        }
        ExecutionDirectory execution = executions.get(name);
        if (execution == null) {
            //
            final File debugFile = new File(rootPath + "/" + name + "/dump/", "debug.json");
            if (!debugFile.exists()) {
                LOG.error("Missing debug file for execution: {}", name);
                // Debug file is missing!
                return null;
            }
            // Initialize execution.
            final DebugStructure debugData;
            try {
                debugData = json.readValue(debugFile, DebugStructure.class);
            } catch (IOException ex) {
                LOG.error("Can't read debug data.", ex);
                return null;
            }
            // Construct the execution record;
            execution = new ExecutionDirectory();
            execution.debugData = debugData;
            for (String entry : debugData.getDataUnits().keySet()) {
                execution.dataUnits.put(entry, null);
            }

            executions.put(name, execution);
        }
        return execution;
    }

    /**
     *
     * @param execution
     * @param name
     * @return Loaded record for data unit directory.
     */
    protected DataUnitDirectory getDataUnitDirectory(ExecutionDirectory execution, String name) {
        if (!execution.dataUnits.containsKey(name)) {
            // Invalid data unit name.
            return null;
        }
        DataUnitDirectory dataUnit = execution.dataUnits.get(name);
        if (dataUnit == null) {
            // Load record.
            final DebugStructure.DataUnit dataUnitDebug = execution.debugData.getDataUnits().get(name);
            if (dataUnitDebug == null) {
                // Invalid name.
                LOG.error("Missing data ({}) unit in list, while it was previously created from this list.",
                        name);
                return null;
            }
            dataUnit = new DataUnitDirectory();
            // Load debug file.
            final File infoFile = new File(new File(URI.create(dataUnitDebug.getDebugDirectory())), "info.dat");
            try {
                for (String line : Files.readLines(infoFile, Charset.forName("UTF-8"))) {
                    dataUnit.directories.add(line + "/");
                }
            } catch (IOException ex) {
                LOG.error("Can't read info.dat file.", ex);
                return null;
            }
            execution.dataUnits.put(name, dataUnit);
        }
        return dataUnit;
    }

    /**
     * Update {@link #executions} list.
     */
    protected void updateExecutionsList() {
        final List<String> newExecutions = executionFacade.getExecutionsIds();
        // Remove.
        final List<String> toRemove = new ArrayList<>(2);
        for (String id : executions.keySet()) {
            if (!newExecutions.contains(id)) {
                toRemove.add(id);
            }
        }
        for (String id : toRemove) {
            executions.remove(id);
        }
        // Add.
        for (String id : executionFacade.getExecutionsIds()) {
            executions.putIfAbsent(id, null);
        }

    }

    /**
     *
     * @param pathList
     * @return String representation of given path.
     */
    protected static String listAsPath(List<String> pathList) {
        if (pathList.isEmpty()) {
            return "/";
        }
        // Construct path.
        final StringBuilder path = new StringBuilder();
        pathList.forEach((item) -> {
            path.append("/");
            path.append(item);
        });
        return path.toString();
    }

    /**
     * Construct user path from given parameters.
     *
     * @param currentDirectory
     * @param fileName
     * @return
     */
    protected static List<String> constructPath(String currentDirectory, String fileName) {
        String normalizedFileName = currentDirectory + '/' + fileName;
        normalizedFileName = normalizedFileName.replace("\\", "/");
        // Replace ., ~ and ..
        final StringTokenizer tokenizer = new StringTokenizer(normalizedFileName, "/");
        final List<String> path = new LinkedList<>();
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            if (".".equals(token)) {
                continue;
            }
            if ("..".equals(token)) {
                // Remove record from result. ie simulate directory up.
                path.remove(path.size() - 1);
                continue;
            }
            path.add(token);
        }
        return path;
    }

}
