package com.linkedpipes.etl.rdf4j;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Statements implements Collection<Statement> {

    private static final Logger LOG = LoggerFactory.getLogger(Statements.class);

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Collection<Statement> collection;

    private IRI defaultGraph;

    public Statements(Collection<Statement> collection) {
        this.collection = collection;
    }

    public static Statements arrayList() {
        return new Statements(new ArrayList<>());
    }

    public static Statements arrayList(File file) throws IOException {
        Statements result = new Statements(new ArrayList<>());
        result.addAll(file);
        return result;
    }

    public static Statements arrayList(int size) {
        return new Statements(new ArrayList<>(size));
    }

    public static Statements emptyReadOnly() {
        return new Statements(Collections.emptyList());
    }

    public void setDefaultGraph(String defaultGraph) {
        this.setDefaultGraph(valueFactory.createIRI(defaultGraph));
    }

    public void setDefaultGraph(IRI defaultGraph) {
        this.defaultGraph = defaultGraph;
    }

    public void addIri(String s, String p, String o) {
        this.addIri(valueFactory.createIRI(s), p, o);
    }

    public void addIri(Resource s, String p, String o) {
        this.add(s, p, valueFactory.createIRI(o));
    }

    public void addIri(String s, IRI p, String o) {
        this.addIri(valueFactory.createIRI(s), p, o);
    }

    public void addIri(Resource s, IRI p, String o) {
        this.add(s, p, valueFactory.createIRI(o));
    }

    public void addString(String s, String p, String o) {
        this.addString(valueFactory.createIRI(s), p, o);
    }

    public void addString(Resource s, String p, String o) {
        this.add(s, p, valueFactory.createLiteral(o));
    }

    public void addString(String s, IRI p, String o) {
        this.add(
                valueFactory.createIRI(s),
                p,
                valueFactory.createLiteral(o));
    }

    public void addString(Resource s, IRI p, String o) {
        this.add(s, p, valueFactory.createLiteral(o));
    }

    public void addInt(String s, String p, int o) {
        this.add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }


    public void addInt(Resource s, String p, int o) {
        this.add(s, p, valueFactory.createLiteral(o));
    }

    public void addInt(Resource s, IRI p, int o) {
        this.add(s, p, valueFactory.createLiteral(o));
    }

    public void addBoolean(String s, String p, boolean o) {
        this.addBoolean(s, valueFactory.createIRI(p), o);
    }

    public void addBoolean(Resource s, String p, boolean o) {
        this.addBoolean(s, valueFactory.createIRI(p), o);
    }

    public void addBoolean(String s, IRI p, boolean o) {
        this.addBoolean(valueFactory.createIRI(s), p, o);
    }

    public void addBoolean(Resource s, IRI p, boolean o) {
        this.add(s, p, valueFactory.createLiteral(o));
    }

    public void addDate(String s, String p, Date o) {
        this.add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void addDate(Resource s, String p, Date o) {
        this.addDate(s, valueFactory.createIRI(p), o);
    }

    public void addDate(Resource s, IRI p, Date o) {
        this.add(s, p, valueFactory.createLiteral(o));
    }

    public void addLong(String s, String p, Long o) {
        this.add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void addLong(Resource s, String p, Long o) {
        this.add(
                s, valueFactory.createIRI(p), valueFactory.createLiteral(o));
    }


    public void add(Resource s, String p, Value o) {
        this.add(s, valueFactory.createIRI(p), o);
    }

    public void add(Resource s, IRI p, Value o) {
        this.collection.add(valueFactory.createStatement(
                s, p, o, this.defaultGraph));
    }

    @Override
    public boolean add(Statement statement) {
        return this.collection.add(statement);
    }

    public void addAll(File file) throws IOException {
        Optional<RDFFormat> format =
                Rio.getParserFormatForFileName(file.getName());
        if (!format.isPresent()) {
            throw new IOException("Can't get format for: " + file.getName());
        }
        this.addAll(file, format.get());
    }

    @Override
    public boolean addAll(Collection<? extends Statement> collection) {
        return this.collection.addAll(collection);
    }

    public void addAll(File file, RDFFormat format) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            this.addAll(stream, format);
        }
    }

    public void addAll(InputStream stream, RDFFormat format)
            throws IOException {
        try {
            RDFParser parser = Rio.createParser(format);
            parser.setRDFHandler(new StatementCollector(this.collection));
            parser.parse(stream, "http://localhost/base");
        } catch (RuntimeException ex) {
            throw new IOException(ex);
        }
    }

    public Statements selectByGraph(String graph) {
        return selectByGraph(valueFactory.createIRI(graph));
    }

    public Statements selectByGraph(IRI graph) {
        Statements result = Statements.arrayList();
        this.collection.stream()
                .filter((st) -> graph.equals(st.getContext()))
                .forEach((st) -> result.collection.add(st));
        return result;
    }

    public StatementsBuilder subject(String subject) {
        return subject(valueFactory.createIRI(subject));
    }

    public StatementsBuilder subject(Resource subject) {
        return new StatementsBuilder(subject, this);
    }

    @Override
    public int size() {
        return this.collection.size();
    }

    @Override
    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    @Override
    public boolean contains(Object object) {
        return this.collection.contains(object);
    }

    @Override
    public Iterator<Statement> iterator() {
        return this.collection.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.collection.toArray(a);
    }

    /**
     * Add statement and update the graph.
     */
    public boolean integrate(Statement statement) {
        Statement integratedStatement = valueFactory.createStatement(
                statement.getSubject(),
                statement.getPredicate(),
                statement.getObject(),
                this.defaultGraph);
        return this.collection.add(integratedStatement);
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.collection.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return this.collection.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return this.collection.retainAll(collection);
    }

    @Override
    public void clear() {
        this.collection.clear();
    }

    public boolean containsAllLogMissing(Collection<Statement> statements) {
        Set<Statement> thisAsSet = new HashSet<>(this.collection);
        boolean containsAll = true;
        for (Statement statement : statements) {
            if (thisAsSet.contains(statement)) {
                continue;
            }
            containsAll = false;
            LOG.info("{}", statement);
        }
        return containsAll;
    }

    public List<Statement> asList() {
        return new ArrayList<>(collection);
    }

}
