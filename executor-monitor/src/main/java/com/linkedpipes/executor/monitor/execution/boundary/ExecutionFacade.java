package com.linkedpipes.executor.monitor.execution.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.commons.entities.executor.CreateExecution;
import java.io.File;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.linkedpipes.commons.entities.rest.ListMetadata;
import com.linkedpipes.commons.entities.rest.RestException;
import com.linkedpipes.commons.entities.executor.MessageSelectList;
import com.linkedpipes.commons.entities.executor.monitor.ExecutionBasic;
import com.linkedpipes.commons.entities.executor.monitor.ExecutionBasicList;
import com.linkedpipes.executor.monitor.Configuration;
import com.linkedpipes.executor.monitor.execution.controller.ExecutionRepository;
import com.linkedpipes.executor.monitor.execution.entity.ExecutionMetadata;
import com.linkedpipes.executor.monitor.execution.entity.InitializeException;
import com.linkedpipes.executor.monitor.execution.util.ExecutionHelper;
import com.linkedpipes.executor.monitor.execution.util.PathDefinitions;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.AbstractRDFHandler;

/**
 *
 * @author Škoda Petr
 */
@Service
public class ExecutionFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionFacade.class);

    @Autowired
    private Configuration configuration;

    @Autowired
    private ExecutionRepository directoryWatcher;

    final ObjectMapper jsonMapper = new ObjectMapper();

    public ExecutionBasic getExecutionBasic(String id, String language) {
        final ExecutionMetadata execution = directoryWatcher.getExecution(id);
        if (execution == null) {
            return null;
        } else {
            return ExecutionHelper.createExecution(execution, language, configuration.getExecutionPrefix(), true);
        }
    }

    /**
     *
     * @param id
     * @return Debug information for given execution or null if no execution exists.
     */
    public ExecutionDebug getExecutionDebug(String id) {
        final ExecutionMetadata execution = directoryWatcher.getExecution(id);
        if (execution == null) {
            return null;
        } else {
            return new ExecutionDebug(execution);
        }
    }

    /**
     *
     * @return IDs of stored executions.
     */
    public List<String> getExecutionsIds() {
        final List<ExecutionMetadata> executions = directoryWatcher.getExecutions();
        final List<String> output = new ArrayList<>(executions.size());
        for (ExecutionMetadata directory : executions) {
            output.add(directory.getId());
        }
        return output;
    }

    /**
     *
     * @param offset
     * @param limit
     * @param language
     * @return Snapshot for basic execution list.
     */
    public ExecutionBasicList getExecutionsBasic(int offset, int limit, String language) {
        final Date createTime = directoryWatcher.getLastUpdate();
        final ExecutionBasicList output = new ExecutionBasicList();
        final List<ExecutionMetadata> data = directoryWatcher.getExecutions();
        output.setMetadata(new ListMetadata(data.size(), createTime));

        final List<ExecutionMetadata> directories = paginate(offset, limit, data);
        final List<ExecutionBasic> executions = new ArrayList<>(directories.size());
        for (ExecutionMetadata item : directories) {
            final ExecutionBasic newExecution = ExecutionHelper.createExecution(item, language,
                    configuration.getExecutionPrefix(), false);
            if (newExecution != null) {
                executions.add(newExecution);
            }
        }
        output.setPayload(executions);
        return output;
    }

    /**
     *
     * @param offset
     * @param limit
     * @param language
     * @param changedSince
     * @return Changes of basic execution list from given time.
     */
    public ExecutionBasicList getExecutionsBasic(int offset, int limit, String language, Date changedSince) {
        final Date createTime = directoryWatcher.getLastUpdate();
        final ExecutionBasicList output = new ExecutionBasicList();
        final List<ExecutionMetadata> data = directoryWatcher.getExecutions(changedSince);
        output.setMetadata(new ListMetadata(data.size(), createTime));

        final List<ExecutionMetadata> directories = paginate(offset, limit, data);
        final List<ExecutionBasic> executions = new ArrayList<>(directories.size());
        for (ExecutionMetadata item : directories) {
            final ExecutionBasic newExecution = ExecutionHelper.createExecution(item, language,
                    configuration.getExecutionPrefix(), false);
            if (newExecution != null) {
                executions.add(newExecution);
            }
        }
        output.setPayload(executions);
        output.setDeleted(directoryWatcher.getDeletedExecutionsId());
        return output;
    }

    /**
     *
     * @param id
     * @return Null in case of missing resources.
     */
    public MessageSelectList selectQueryMessages(String id) {
        final Date createDate = new Date();
        final ExecutionMetadata execution = directoryWatcher.getExecution(id);
        if (execution == null) {
            return null;
        }
        // TODO We can list some list of running executions instead or easy running check.
        final ExecutionBasic basic = ExecutionHelper.createExecution(execution, "en",
                configuration.getExecutionPrefix(), false);
        if (basic.isRunning()) {
            // Redirect query to the executor.
            final RestTemplate restTemplate = new RestTemplate();

            final ResponseEntity<MessageSelectList> response;

            try {
                response = restTemplate.getForEntity(
                        java.net.URI.create(configuration.getExecutorUri() + "/api/v1/executions/messages"),
                        MessageSelectList.class);
                if (response.getBody() != null) {
                    return response.getBody();
                }
            } catch (Exception ex) {
                LOG.warn("Failed to read messages from exectur.", ex);
            }
        }
        // Load from file.
        final File messagesFile = new File(execution.getDirectory(), PathDefinitions.MESSAGES);
        if (!messagesFile.exists()) {
            return new MessageSelectList(new RestException(
                    "",
                    "Missing message file!",
                    "Data are not available!",
                    RestException.Codes.ERROR));
        }
        //
        final Map<Resource, Map<String, Object>> data = new HashMap<>();

        final Optional<RDFFormat> format = Rio.getParserFormatForFileName(messagesFile.getName());
        final RDFParser parser = Rio.createParser(format.get(), SimpleValueFactory.getInstance());

        parser.setRDFHandler(new AbstractRDFHandler() {

            @Override
            public void handleStatement(Statement st) throws RDFHandlerException {
                Map<String, Object> message = data.get(st.getSubject());
                if (message == null) {
                    message = MessageSelectList.create(st.getSubject().stringValue());
                    data.put(st.getSubject(), message);
                }
                MessageSelectList.addProperty(message, st.getPredicate().stringValue(), st.getObject().stringValue());
            }
        });

        try (InputStream stream = new FileInputStream(messagesFile)) {
            parser.parse(stream, "http://localhost/");
        } catch (IOException ex) {
            LOG.error("Can't parse message graph.", ex);
            return new MessageSelectList(new RestException(
                    "",
                    "Can't read message file!",
                    "Data are not available!",
                    RestException.Codes.ERROR));
        }
        //
        final ListMetadata metadata = new ListMetadata(data.size(), createDate);
        final MessageSelectList result = new MessageSelectList(metadata, new ArrayList<>(data.values()));
        return result;
    }

    /**
     *
     * @param id
     * @return Path to execution messages dump file, or null if no such execution or file exists.
     */
    public File getExecutionMessagesFile(String id) {
        final ExecutionMetadata execution = directoryWatcher.getExecution(id);
        if (execution == null) {
            return null;
        }
        final File messagesFile = new File(execution.getDirectory(), PathDefinitions.MESSAGES);
        if (!messagesFile.exists()) {
            return null;
        }
        return messagesFile;
    }

    /**
     *
     * @param id
     * @return Path to execution messages dump file, or null if no such execution or file exists.
     */
    public File getExecutionLogsFile(String id) {
        final ExecutionMetadata execution = directoryWatcher.getExecution(id);
        if (execution == null) {
            return null;
        }
        final File logsFile = new File(execution.getDirectory(), PathDefinitions.LOGS);
        if (!logsFile.exists()) {
            return null;
        }
        return logsFile;
    }

    /**
     *
     * @param id
     * @return Path to debug JSON file, or null if no such execution or file exists.
     */
    public File getExecutionDebugFile(String id) {
        final ExecutionMetadata execution = directoryWatcher.getExecution(id);
        if (execution == null) {
            return null;
        }
        final File debugFile = new File(execution.getDirectory(), PathDefinitions.DEBUG);
        if (!debugFile.exists()) {
            return null;
        }
        return debugFile;
    }

    /**
     *
     * @param id
     * @return Path to JSON file with labels.
     */
    public File getExecutionLabelFile(String id) {
        final ExecutionMetadata execution = directoryWatcher.getExecution(id);
        if (execution == null) {
            return null;
        }
        final File debugFile = new File(execution.getDirectory(), PathDefinitions.LABELS);
        if (!debugFile.exists()) {
            return null;
        }
        return debugFile;
    }

    /**
     * Delete given execution. The tombstone is created for deleted execution.
     *
     * @param id
     */
    public void deleteExecution(String id) {
        final ExecutionMetadata execution = directoryWatcher.getExecution(id);
        if (execution != null) {
            directoryWatcher.deleteExecution(execution);
        }
    }

    /**
     * Create execution for given stream.
     *
     * @param inputStream
     * @param extension
     * @return
     * @throws InitializeException
     */
    public CreateExecution createExecution(InputStream inputStream, String extension) throws InitializeException {
        final String id = directoryWatcher.createExecution(inputStream, extension);
        return new CreateExecution(id);
    }

    /**
     *
     * @param offset
     * @param limit
     * @param data
     * @return New list with sub-list of defined range only.
     */
    protected List<ExecutionMetadata> paginate(int offset, int limit, List<ExecutionMetadata> data) {
        final int to = offset + limit > data.size() ? data.size() : offset + limit;
        return data.subList(offset, to);
    }

}
