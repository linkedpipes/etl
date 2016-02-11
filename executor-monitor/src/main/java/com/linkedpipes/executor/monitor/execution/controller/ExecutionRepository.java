package com.linkedpipes.executor.monitor.execution.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.commons.entities.executor.CreateExecution;
import com.linkedpipes.commons.entities.executor.Labels;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.linkedpipes.executor.monitor.Configuration;
import com.linkedpipes.executor.monitor.execution.entity.ExecutionMetadata;
import com.linkedpipes.executor.monitor.execution.entity.ExecutionMetadata.Status;
import com.linkedpipes.executor.monitor.execution.entity.InitializeException;
import com.linkedpipes.executor.monitor.execution.util.ExecutionHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Škoda Petr
 */
@Service
public final class ExecutionRepository {

    /**
     * Used to hold value for inner classes.
     *
     * @param <T>
     */
    private static class ValueHolder<T> {

        T value;

    }

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionRepository.class);

    @Autowired
    private Configuration configuration;

    /**
     * Sorted list of records.
     */
    private final List<ExecutionMetadata> executions = new ArrayList<>(64);

    /**
     * List of directories to delete.
     * The execution directory might be used by some user (reading logs, etc .. ) so we store request to delete
     * here and we will try until we manage to delete the data.
     */
    private final List<ExecutionMetadata> toDelete = new ArrayList<>(16);

    /**
     * Store list of deleted executions identifications. The date determines when the tombstone will be removed.
     */
    private final Map<String, Date> deletedExecutionsId = new HashMap<>();

    /**
     * Date of last update, ie. all data are updated at least up to this date.
     */
    private Date lastUpdate = null;

    private final ObjectMapper jacksonMapper = new ObjectMapper();

    @PostConstruct
    protected void onInit() {
        // Update and load new.
        final Date updateTime = new Date();
        for (final File directory : configuration.getWorkingDirectory().listFiles()) {
            if (!directory.isDirectory()) {
                continue;
            }
            final File taskFile = new File(directory, "task.json");
            final ExecutionMetadata execution;
            try {
                execution = jacksonMapper.readValue(taskFile, ExecutionMetadata.class);
            } catch (IOException ex) {
                LOG.error("Can't read task file for directory: {}", directory, ex);
                continue;
            }
            try {
                readLabels(execution.getDefinitionFile(), execution);
            } catch (InitializeException ex) {
                LOG.error("Can't read labels for directory: {}", directory, ex);
                continue;
            }
            executions.add(execution);
        }
        lastUpdate = updateTime;
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 500)
    protected void check() {
        final Date now = new Date();
        // Delete.
        for (Iterator<ExecutionMetadata> iterator = toDelete.iterator(); iterator.hasNext();) {
            final ExecutionMetadata entry = iterator.next();
            if (FileUtils.deleteQuietly(entry.getDirectory())) {
                iterator.remove();
            }
        }
        // Check tombstones lifetime.
        for (Iterator<Map.Entry<String, Date>> iterator = deletedExecutionsId.entrySet().iterator(); iterator.hasNext();) {
            final Map.Entry<String, Date> entry = iterator.next();
            if (now.after(entry.getValue())) {
                iterator.remove();
            }
        }
        // Try to start new tasks - for now we assume we have exactly one executor.
        boolean running = false;
        for (ExecutionMetadata item : executions) {
            if (item.getStatus() == Status.RUNNING) {
                // Update execution.
                ExecutionHelper.updateExecution(item);
                if (item.getStatus() == Status.RUNNING) {
                    running = true;
                }
            }
        }
        if (!running) {
            final List<ExecutionMetadata> queue = getQueuedExecutions();
            if (!queue.isEmpty()) {
                LOG.info("Staring execution");
                // Start new execution.
                final ExecutionMetadata executionToRun = queue.get(0);
                try {
                    startExecution(executionToRun);
                } catch (JsonProcessingException | RuntimeException ex) {
                    LOG.info("Can't start execution!", ex);
                }
                // Save the record.
                final File taskFile = new File(executionToRun.getDirectory(), "task.json");
                try (OutputStream outputStream = Files.newOutputStream(taskFile.toPath())) {
                    jacksonMapper.writeValue(outputStream, executionToRun);
                } catch (IOException ex) {
                    // TODO We should try later, as otherwise
                    LOG.error("Can't write task file!", ex);
                }
            }
        }
        // Change time of last update to current.
        lastUpdate = now;
    }

    /**
     * Class used to start task in executor.
     */
    private static class TaskToExecute {

        String directory;

        String executionId;

        public TaskToExecute(String directory, String executionId) {
            this.directory = directory;
            this.executionId = executionId;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getExecutionId() {
            return executionId;
        }

        public void setExecutionId(String executionId) {
            this.executionId = executionId;
        }

    }

    protected void startExecution(ExecutionMetadata execution) throws JsonProcessingException, RestClientException {
        final TaskToExecute task = new TaskToExecute(execution.getDirectory().toString(), execution.getId());
        final String body = jacksonMapper.writeValueAsString(task);
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");

        final HttpEntity<String> entity = new HttpEntity<>(body, headers);

        final URI uri = URI.create(configuration.getExecutorUri() + "/api/v1/executions");
        final RestTemplate restTemplate = new RestTemplate();

        final ResponseEntity<CreateExecution> responseEntity;
        responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, CreateExecution.class);

        execution.setExecutorAddress(uri.toString());
        execution.setStatus(Status.RUNNING);
//        // org.springframework.web.client.ResourceAccessException: I/O
//        // org.springframework.web.client.HttpServerErrorException: 500 Server Error
    }

    /**
     * Create record of a new execution.
     *
     * @param inputStream
     * @param extension
     * @return
     * @throws InitializeException
     */
    public String createExecution(InputStream inputStream, String extension) throws InitializeException {
        final String uuid = UUID.randomUUID().toString();
        final File workingDirectory = new File(configuration.getWorkingDirectory(), uuid);
        final File definitionFile = new File(workingDirectory, "definition" + File.separator + "definition." + extension);
        definitionFile.getParentFile().mkdirs();
        // Save stream content.
        try {
            Files.copy(inputStream, definitionFile.toPath());
        } catch (IOException exception) {
            throw new InitializeException("Can't save pipeline definition.", exception);
        }
        // Save task info file -> and start executions.
        final ExecutionMetadata newRecord = new ExecutionMetadata(uuid, workingDirectory, definitionFile);
        // Read metadata from execution.
        readLabels(definitionFile, newRecord);
        // Write information to the file.
        final File taskFile = new File(workingDirectory, "task.json");
        try (OutputStream outputStream = Files.newOutputStream(taskFile.toPath())) {
            jacksonMapper.writeValue(outputStream, newRecord);
        } catch (IOException ex) {
            throw new InitializeException("Can't write task file!", ex);
        }
        executions.add(newRecord);
        return uuid;
    }

    /**
     *
     * @param execution Delete given directory.
     */
    public void deleteExecution(ExecutionMetadata execution) {
        if (!executions.remove(execution)) {
            // No such execution exists.
            return;
        }
        toDelete.add(execution);
        final Calendar removeTombstone = Calendar.getInstance();
        removeTombstone.add(Calendar.MINUTE, 15);
        deletedExecutionsId.put(execution.getId(), removeTombstone.getTime());
    }

    /**
     *
     * @param id
     * @return Execution directory record for given ID, or null if for given ID there is no executions.
     */
    public ExecutionMetadata getExecution(String id) {
        for (ExecutionMetadata item : executions) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     *
     * @return Return all queued executions.
     */
    protected List<ExecutionMetadata> getQueuedExecutions() {
        final List<ExecutionMetadata> resultList = new ArrayList<>(16);
        for (ExecutionMetadata item : executions) {
            if (item.getStatus() == Status.QUEUED) {
                resultList.add(item);
            }
        }
        return resultList;
    }

    /**
     *
     * @return Unmodifiable list of all stored executions.
     */
    public List<ExecutionMetadata> getExecutions() {
        return Collections.unmodifiableList(executions);
    }

    /**
     *
     * @param changedSince
     * @return List of all executions that has been updates since given time.
     */
    public List<ExecutionMetadata> getExecutions(Date changedSince) {
        final List<ExecutionMetadata> data = new ArrayList<>(16);
        for (ExecutionMetadata record : executions) {
            if (record.getUpdateTime().after(changedSince)) {
                data.add(record);
            }
        }
        return data;
    }

    /**
     *
     * @return A set of recently deleted executions identifiers.
     */
    public Set<String> getDeletedExecutionsId() {
        return deletedExecutionsId.keySet();
    }

    /**
     *
     * @return Time of last change on any execution.
     */
    public Date getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Read metadata from execution definition file.
     *
     * @param definitionFile
     * @param execution
     */
    private void readLabels(File definitionFile, ExecutionMetadata execution) throws InitializeException {
        // Load labels.
        final Labels labels = new Labels();
        // Used to store detected pipeline resource.
        final ValueHolder<Resource> pipelineResource = new ValueHolder<>();
        final Optional<RDFFormat> format = Rio.getParserFormatForFileName(definitionFile.getName());
        if (!format.isPresent()) {
            throw new InitializeException("Unknown definition format: '" + definitionFile.getName() + "'");
        }
        final RDFParser parser = Rio.createParser(format.get());
        parser.setRDFHandler(new AbstractRDFHandler() {
            @Override
            public void handleStatement(Statement statement) throws RDFHandlerException {
                if (SKOS.PREF_LABEL.equals(statement.getPredicate()) && statement.getObject() instanceof Literal) {
                    final Literal literal = (Literal) statement.getObject();
                    final String language = literal.getLanguage().orElse("");
                    labels.addLabel(statement.getSubject().stringValue(), language, literal.stringValue());
                } else if (RDF.TYPE.equals(statement.getPredicate())
                        && statement.getObject().stringValue().equals("http://linkedpipes.com/ontology/Pipeline")) {
                    // TODO Vocabulary reference!
                    pipelineResource.value = statement.getSubject();
                }
            }
        });
        try (InputStream inputStream = Files.newInputStream(definitionFile.toPath())) {
            parser.parse(inputStream, "http://localhost/temp/resources/");
        } catch (RDFHandlerException | RDFParseException | IOException ex) {
            throw new InitializeException("Can't read definition file.", ex);
        }
        // TODO File reference!
        final File file = new File(execution.getDirectory(), "dump" + File.separator + "labels.json");
        file.getParentFile().mkdirs();
        try {
            jacksonMapper.writeValue(file, labels);
        } catch (IOException ex) {
            throw new InitializeException("Can't write pipeline labels file!", ex);
        }
        // Store labels to the metadata record, we do this here so if the writing of labels fail,
        // no labels are stored to the execution - so it's same as after restart.
        if (pipelineResource.value == null) {
            throw new InitializeException("Missing pipeline resource in definition!");
        } else {
            execution.setPipelineUri(pipelineResource.value.stringValue());
            execution.setLabel(labels.getLabels(pipelineResource.value.stringValue()));
        }
    }

}
