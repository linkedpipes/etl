package com.linkedpipes.executor.rdf.boundary;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.commons.entities.rest.RestException;
import com.linkedpipes.commons.entities.executor.MessageSelectList;
import com.linkedpipes.commons.entities.rest.ListMetadata;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.executor.rdf.controller.ConnectionAction;
import com.linkedpipes.executor.rdf.util.SesameUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryResult;

/**
 *
 * @author Škoda Petr
 */
public class MessageStorage {

    private static final Logger LOG = LoggerFactory.getLogger(MessageStorage.class);

    public static interface MessageListener {

        public void onMesssage(Event message);

    }

    private final Repository repository;

    private final String messageUriPrefix;

    private final List<WeakReference<MessageListener>> listeners = new LinkedList<>();

    private final String graphUri;

    private long counter = 0;

    public MessageStorage(File workingDir, String executionUri) throws RepositoryException {
        this.repository = new SailRepository(new NativeStore(workingDir));
        this.repository.initialize();
        this.messageUriPrefix = executionUri + "/messages/";
        this.graphUri = executionUri + "/messages";
    }

    public void store(File file, RDFFormat format) {
        try {
            ConnectionAction.call(repository, (connection) -> {
                SesameUtils.store(connection, file, format);
            });
        } catch (ConnectionAction.CallFailed ex) {
            LOG.info("Can't store message DataUnit.", ex);
        }
    }

    public void close() {
        try {
            repository.shutDown();
        } catch (RepositoryException ex) {
            LOG.error("Can't close repository.", ex);
        }
    }

    /**
     *
     * @return New and unique subject.
     */
    protected String createNewSubject() {
        return messageUriPrefix + Long.toString(++counter);
    }

    public void addListener(MessageListener listener) {
        listeners.add(new WeakReference(listener));
    }

    /**
     * Synchronized method as the used repository may not be thread save.
     *
     * @param message
     */
    public synchronized void publish(Event message) {
        // Assign subject to the message.
        message.assignSubject(createNewSubject());
        // Notify listeners.
        final List<WeakReference<MessageListener>> nullReferences = new LinkedList();
        for (WeakReference<MessageListener> reference : listeners) {
            final MessageListener listener = reference.get();
            if (listener != null) {
                listener.onMesssage(message);
            } else {
                nullReferences.add(reference);
            }
        }
        listeners.removeAll(nullReferences);
        // Store message - this can fail but should not.
        final WritableRdfJava writer = new WritableRdfJava(repository, graphUri);
        writer.begin();
        message.write(writer);
        try {
            writer.commit();
        } catch (RdfOperationFailed ex) {
            LOG.error("Can't save message!", ex);
        }
    }

    public MessageSelectList getMessages() {
        if (!repository.isInitialized()) {
            return new MessageSelectList(new RestException(
                    "",
                    "Repository is no longer available!",
                    "Data are temporarily inavailable!",
                    RestException.Codes.RETRY));
        }
        //
        final Map<Resource, Map<String, Object>> data = new HashMap<>();
        final Date createTime = new Date();
        RepositoryConnection connection = null;
        try {
            connection = repository.getConnection();
            final RepositoryResult<Statement> result = connection.getStatements(null, null, null);
            while(result.hasNext()) {
                final Statement st = result.next();
                Map<String, Object> message = data.get(st.getSubject());
                if (message == null) {
                    message = MessageSelectList.create(st.getSubject().stringValue());
                    data.put(st.getSubject(), message);
                }
                MessageSelectList.addProperty(message, st.getPredicate().stringValue(), st.getObject().stringValue());
            }
        } catch (OpenRDFException ex) {
            LOG.error("Can't create message list.", ex);
            return new MessageSelectList(new RestException(
                    "",
                    "",
                    "Invalid query!",
                    RestException.Codes.INVALID_INPUT));
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection.", ex);
                }
            }
        }
        //
        final ListMetadata metadata = new ListMetadata(data.size(), createTime);
        final MessageSelectList result = new MessageSelectList(metadata, new ArrayList<>(data.values()));
        return result;
    }

}
