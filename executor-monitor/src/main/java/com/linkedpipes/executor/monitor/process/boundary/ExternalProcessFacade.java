package com.linkedpipes.executor.monitor.process.boundary;

import com.linkedpipes.commons.entities.executor.monitor.ExternalProcess;
import com.linkedpipes.commons.entities.executor.monitor.ExternalProcessBasicList;
import com.linkedpipes.commons.entities.executor.monitor.ExternalProcessEntity;
import com.linkedpipes.commons.entities.rest.RestException;
import com.linkedpipes.executor.monitor.Configuration;
import com.linkedpipes.executor.monitor.execution.boundary.ExecutionDebug;
import com.linkedpipes.executor.monitor.execution.boundary.ExecutionFacade;
import com.linkedpipes.executor.monitor.process.entity.BaseProcess;
import com.linkedpipes.executor.monitor.process.entity.FusekiProcess;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Service;

/**
 *
 * @author Petr Škoda
 */
@Service
public class ExternalProcessFacade implements ApplicationListener<ContextStoppedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalProcessFacade.class);

    @Autowired
    private ExecutionFacade executionFacade;

    @Autowired
    private Configuration configuration;

    /**
     * Store used ports.
     */
    private final Set<Integer> usedPorts = new HashSet<>();

    /**
     * Storage for running processes.
     */
    private final List<BaseProcess> processes = new ArrayList<>();

    /**
     * Used to generate process ID.
     */
    private long idCounter = 0;

    /**
     * Lock used to access {@link #idCounter}.
     */
    private final Object idCounterLock = new Object();

    /**
     *
     * @return List of all stored processes.
     */
    public ExternalProcessBasicList getProcesses() {
        final List<ExternalProcess> outputList = new ArrayList<>(processes.size());
        for (BaseProcess entry : processes) {
            outputList.add(entry.getExternalProcess());
        }
        // Create output entity.
        final ExternalProcessBasicList output = new ExternalProcessBasicList();
        output.setPayload(outputList);
        return output;
    }

    /**
     * Terminate given process.
     *
     * @param id
     */
    public void terminate(String id) {
        for (BaseProcess process : processes) {
            if (process.getExternalProcess().getId().equals(id)) {
                terminate(process);
                processes.remove(process);
                return;
            }
        }
    }

    /**
     * Start Fuseki server for content of given data unit and return object that can be used to manipulate with this
     * process.
     *
     * @param executionUri
     * @param dataUnitId
     * @return Never return null.
     */
    public ExternalProcessEntity startFuseki(String executionUri, String dataUnitId) {
        // Get data file.
        final ExecutionDebug debug = executionFacade.getExecutionDebug(executionUri);
        if (debug == null) {
            final ExternalProcessEntity result = new ExternalProcessEntity();
            result.setException(new RestException(
                    "",
                    "Execution does not exist.",
                    "Execution does not exist.",
                    RestException.Codes.INVALID_INPUT));
            return result;
        }
        final File rdfFile;
        try {
            rdfFile = debug.getRdfDump(dataUnitId);
        } catch (ExecutionDebug.InvalidResource ex) {
            LOG.error("Can't get rdf file.", ex);
            final ExternalProcessEntity result = new ExternalProcessEntity();
            result.setException(new RestException(
                    "",
                    "No RDF data file found for data unit: " + dataUnitId + " for execution: " + executionUri,
                    "Invalid data unit.",
                    RestException.Codes.INVALID_INPUT));
            return result;
        }
        // Copy directory.
        final File originalFusekiDirectory = configuration.getFusekiPath();
        final File workingRoot = configuration.getExternalWorkingDirectoryPath();
        //
        String processId = null;
        File fusekiDirectory = null;
        // Find a directory that does not exists.
        for (int i = 0; i < 10000; ++i) {
            processId = createProcessId();
            final File newFusekiDirectory = new File(workingRoot, "Fuseki-" + processId);
            if (!newFusekiDirectory.exists()) {
                fusekiDirectory = newFusekiDirectory;
                fusekiDirectory.mkdirs();
                break;
            }
        }
        if (fusekiDirectory == null) {
            final ExternalProcessEntity result = new ExternalProcessEntity();
            result.setException(new RestException(
                    "",
                    "Can't obtain working directory.",
                    "Can't obtain working directory.",
                    RestException.Codes.RETRY));
            return result;
        }
        // Copy content.
        try {
            FileUtils.copyDirectory(originalFusekiDirectory, fusekiDirectory);
        } catch (IOException ex) {
            FileUtils.deleteQuietly(fusekiDirectory);
            LOG.error("Can't copy Fuseki directory.", ex);
            final ExternalProcessEntity result = new ExternalProcessEntity();
            result.setException(new RestException(
                    "",
                    "Can't initialize Fuseki directory.",
                    "Can't initialize Fuseki directory.",
                    RestException.Codes.RETRY));
            return result;
        }
        // Start process.
        final Integer port = getPort();
        if (port == null) {
            final ExternalProcessEntity result = new ExternalProcessEntity();
            result.setException(new RestException(
                    "",
                    "No free port to use.",
                    "No free port to use by the service, please try again later.",
                    RestException.Codes.RETRY));
            return result;
        }
        final String datasetName = "Debug";
        final ProcessBuilder builder = new ProcessBuilder();

//        builder.redirectErrorStream(true);
//        builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
//        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        builder.directory(fusekiDirectory);
        builder.command(
                "java",
                "-Xmx1g",
                "-jar",
                "fuseki-server.jar",
                "--file=" + rdfFile.getPath(),
                "--port=" + port.toString(),
                "/" + datasetName);
        final Process process;
        try {
            process = builder.start();
        } catch (IOException ex) {
            // Release port.
            usedPorts.remove(port);
            //
            LOG.error("Can't start process.", ex);
            final ExternalProcessEntity result = new ExternalProcessEntity();
            result.setException(new RestException(
                    "",
                    "Can't start process.",
                    "Can't start external process.",
                    RestException.Codes.ERROR));
            return result;
        }
        if (!process.isAlive()) {
            // Failed to start process.
            // TODO Store error, output stream?
            usedPorts.remove(port);
            LOG.error("Can't start process.");
            final ExternalProcessEntity result = new ExternalProcessEntity();
            result.setException(new RestException(
                    "",
                    "Can't start process.",
                    "Can't start external process.",
                    RestException.Codes.ERROR));
            return result;
        }
        // Create output objects.
        final ExternalProcess externalProcess = new ExternalProcess();
        externalProcess.setCommand(listToString(builder.command()));
        externalProcess.setCreated(new Date().getTime());
        externalProcess.setDescription("Fuseki server");
        externalProcess.setId(processId);
        externalProcess.setLinkToService("http://localhost:" + Integer.toString(port)
                + "/dataset.html?tab=query&ds=/" + datasetName);
        //
        final BaseProcess reference = new FusekiProcess(fusekiDirectory, externalProcess, process, port);
        processes.add(reference);
        // Return.
        final ExternalProcessEntity result = new ExternalProcessEntity();
        result.setPayload(externalProcess);
        return result;
    }

    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        // Kill all on exit.
        for (BaseProcess proces : processes) {
            terminate(proces);
        }
        processes.clear();
    }

    /**
     * Terminate given process. Does not remove process from the {@link #processes} list.
     *
     * @param process
     */
    protected void terminate(BaseProcess process) {
        // Terminate process.
        process.terminate();
        // Remove port from the user port list.
        usedPorts.remove(process.getPort());
    }

    /**
     *
     * @return New port that can be used, null if there is no free port.
     */
    protected Integer getPort() {
        synchronized (usedPorts) {
            for (int i = configuration.getProcessPortStart(); i < configuration.getProcessPortEnd(); ++i) {
                if (!usedPorts.contains(i)) {
                    usedPorts.add(i);
                    return i;
                }
            }
        }
        return null;
    }

    /**
     *
     * @return Unique process id.
     */
    protected String createProcessId() {
        synchronized (idCounterLock) {
            return Long.toString(++idCounter);
        }
    }

    /**
     * Convert list to string.
     *
     * @param list
     * @return
     */
    protected static String listToString(List<String> list) {
        String output = "";
        for (String item : list) {
            output += item + " ";
        }
        return output;
    }

}
