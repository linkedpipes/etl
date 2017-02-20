package com.linkedpipes.etl.rdf.utils.rdf4j;

import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A specific implementation of a source class.
 */
public class Rdf4jSource implements RdfSource<Value> {

    @FunctionalInterface
    private interface Converter<ValueType> {

        ValueType convert(Value value);

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(Rdf4jSource.class);

    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    private final Repository repository;

    /**
     * If true the repository is closed upon shutdown of the source.
     */
    private final boolean closeOnShutdown;

    /**
     *
     * @param repository Initialized repository.
     * @param closeOnShutdown True if shutdown the repository on close, ie.
     * the repository is not shared by multiple sources.
     */
    protected Rdf4jSource(Repository repository, boolean closeOnShutdown) {
        this.repository = repository;
        this.closeOnShutdown = closeOnShutdown;
    }

    @Override
    public Class<Value> getDefaultType() {
        return Value.class;
    }

    @Override
    public void shutdown() {
        if (closeOnShutdown) {
            repository.shutDown();
        }
    }

    @Override
    public TripleWriter getTripleWriter(String graph) {
        return new BufferedTripleWriter(graph, repository);
    }

    @Override
    public TypedTripleWriter<Value> getTypedTripleWriter(String graph) {
        return new BufferedTripleWriter(graph, repository);
    }

    @Override
    public ValueInfo<Value> getValueInfo() {
        return (value) -> value instanceof Resource;
    }

    @Override
    public <ValueType> void triples(String resource, String graph,
            Class<ValueType> clazz, TripleHandler<ValueType> handler)
            throws RdfUtilsException {
        final Converter<ValueType> converter = getConverter(clazz);
        if (converter == null) {
            throw new RdfUtilsException("Missing converter to: {}",
                    clazz.getName());
        }
        try (RepositoryConnection connection = repository.getConnection()) {
            Resource subject = null;
            if (resource != null) {
                subject = VF.createIRI(resource);
            }
            final RepositoryResult<Statement> result =
                    connection.getStatements(subject,
                            null, null, VF.createIRI(graph));
            while (result.hasNext()) {
                final Statement s = result.next();
                handler.handle(s.getSubject().stringValue(),
                        s.getPredicate().stringValue(),
                        converter.convert(s.getObject()));
            }
        }
    }

    @Override
    public ValueConverter<Value> valueConverter() {
        return new Rdf4jConverter();
    }

    @Override
    public <T> ValueToString<T> toStringConverter(Class<T> clazz) {
        ValueToString<T> converter = null;
        if (clazz.equals(String.class)) {
            converter = value -> (String) value;
        } else if (clazz.equals(Value.class)) {
            converter = value -> ((Value) value).stringValue();
        }
        return converter;
    }

    @Override
    public <T> List<Map<String, T>> sparqlSelect(String query, Class<T> clazz)
            throws RdfUtilsException {
        final Converter<T> converter = getConverter(clazz);
        final List<Map<String, T>> output = new LinkedList<>();
        Repositories.consume(repository, (connection) -> {
            final TupleQueryResult result;
            try {
                result = connection.prepareTupleQuery(query).evaluate();
            } catch (RuntimeException ex) {
                LOG.info("Failed query: {}", query);
                throw ex;
            }
            while (result.hasNext()) {
                final Map<String, T> item = new HashMap<>();
                final BindingSet bindingSet = result.next();
                for (Binding binding : bindingSet) {
                    item.put(binding.getName(),
                            converter.convert(binding.getValue()));
                }
                output.add(item);
            }
        });
        return output;
    }

    public Repository getRepository() {
        return repository;
    }

    /**
     * @return RDF4J source backed up with InMemory store.
     */
    public static Rdf4jSource createInMemory() {
        final Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        return new Rdf4jSource(repository, true);
    }

    /**
     * @param repository
     * @return Wrap of given repository.
     */
    public static Rdf4jSource createWrap(Repository repository) {
        return new Rdf4jSource(repository, false);
    }

    /**
     * Converter from value to given type.
     *
     * @param clazz
     * @param <ValueType>
     * @return
     */
    private static <ValueType> Converter<ValueType> getConverter(
            Class<ValueType> clazz) {
        Converter converter = null;
        if (clazz.equals(String.class)) {
            converter = (value) -> value.stringValue();
        } else if (clazz.equals(Value.class)) {
            converter = (value) -> value;
        }
        return converter;
    }

}
