package cz.skodape.hdt.rdf.rdf4j;

import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.Reference;
import cz.skodape.hdt.core.ReferenceSource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The graph of the root source entity is kept in sync with the loaded model,
 * i.e. only data from a single graph are loaded at a time.
 */
public class Rdf4jChunkedSource
        extends Rdf4jSource implements ReferenceSource {

    private static class State {

        /**
         * Represent a current data model.
         */
        List<Statement> statements = null;

        List<Reference> roots = null;

        Resource graph = null;

        int rootsIndex = 0;

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(Rdf4jChunkedSource.class);

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Rdf4jChunkedSourceConfiguration configuration;

    private State current = null;

    /**
     * If not null must contain roots.
     */
    private State next = null;

    private Rdf4jGraphProducer producer = null;

    private boolean producerIsEmpty = false;

    private Thread producerThread;

    public Rdf4jChunkedSource(
            Rdf4jChunkedSourceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void open() throws OperationFailed {
        producer = new Rdf4jGraphProducer(configuration.file);
        producerThread = new Thread(producer);
        producerThread.setName("rdf4j-graph-producer");
        producerThread.start();
    }

    @Override
    public void close() {
        producerThread.interrupt();
        try {
            producerThread.join();
        } catch (InterruptedException exception) {
            LOG.info("Interrupted");
        }
    }

    @Override
    public ReferenceSource roots() {
        return this;
    }

    @Override
    protected List<Value> property(
            Resource graph, Resource resource, String property) {
        if (current == null) {
            return Collections.emptyList();
        }
        if (!current.graph.equals(graph)) {
            LOG.warn("Requested data from non-root graph.");
            return Collections.emptyList();
        }
        IRI predicate = valueFactory.createIRI(property);
        List<Value> result = new ArrayList<>();
        for (var statement : current.statements) {
            if (!resource.equals(statement.getSubject())) {
                continue;
            }
            if (!predicate.equals(statement.getPredicate())) {
                continue;
            }
            result.add(statement.getObject());
        }
        return result;
    }

    @Override
    public ReferenceSource split() throws OperationFailed {
        throw new OperationFailed("Can't split root iterator.");
    }

    @Override
    public Reference next() {
        if (nextReadyInCurrent()) {
            return current.roots.get(current.rootsIndex++);
        }
        if (next == null) {
            prepareNextState();
        }
        current = next;
        next = null;
        if (nextReadyInCurrent()) {
            return current.roots.get(current.rootsIndex++);
        }
        return null;
    }

    protected boolean nextReadyInCurrent() {
        return current != null && current.rootsIndex < current.roots.size();
    }

    protected void prepareNextState() {
        if (next != null || producerIsEmpty) {
            return;
        }
        while (true) {
            Rdf4jGraphProducer.Container container =
                    gatStatementsFromProducer();
            if (container == null || container.statements == null) {
                producerIsEmpty = true;
                return;
            }
            State newState = new State();
            newState.statements = container.statements;
            newState.roots = collectRoots(container.statements);
            newState.graph = container.graph;
            LOG.debug("New next state with {} roots in {} statements.",
                    newState.roots.size(), newState.statements.size());
            if (newState.roots.size() == 0) {
                continue;
            }
            next = newState;
            break;
        }
    }

    protected Rdf4jGraphProducer.Container gatStatementsFromProducer() {
        try {
            return producer.getQueue().take();
        } catch (InterruptedException exception) {
            LOG.info("Interrupted");
            return null;
        }
    }

    protected List<Reference> collectRoots(List<Statement> statements) {
        Set<ResourceInGraph> subjects = new HashSet<>();
        statements.stream()
                .map(ResourceInGraph::new)
                .forEach(subjects::add);
        return subjects.stream()
                .map(item -> (Reference) this.wrap(item.graph, item.resource))
                .collect(Collectors.toList());

    }

}
