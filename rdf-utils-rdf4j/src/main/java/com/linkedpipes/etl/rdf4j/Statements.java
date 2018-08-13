package com.linkedpipes.etl.rdf4j;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Statements implements Collection<Statement> {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Collection<Statement> collection;

    private IRI defaultGraph;

    public Statements(Collection<Statement> collection) {
        this.collection = collection;
    }

    public static Statements ArrayList() {
        return new Statements(new ArrayList<>());
    }

    public static Statements ArrayList(int size) {
        return new Statements(new ArrayList<>(size));
    }

    public static Statements EmptyReadOnly() {
        return new Statements(Collections.emptyList());
    }

    public void setDefaultGraph(String defaultGraph) {
        this.setDefaultGraph(this.valueFactory.createIRI(defaultGraph));
    }

    public void setDefaultGraph(IRI defaultGraph) {
        this.defaultGraph = defaultGraph;
    }

    public void addIri(String s, String p, String o) {
        this.addIri(this.valueFactory.createIRI(s), p, o);
    }


    public void addIri(Resource s, String p, String o) {
        this.add(s, p, this.valueFactory.createIRI(o));
    }

    public void addIri(Resource s, IRI p, String o) {
        this.add(s, p, this.valueFactory.createIRI(o));
    }

    public void addString(String s, String p, String o) {
        this.addString(this.valueFactory.createIRI(s), p, o);
    }

    public void addString(Resource s, String p, String o) {
        this.add(s, p, this.valueFactory.createLiteral(o));
    }

    public void addString(Resource s, IRI p, String o) {
        this.add(s, p, this.valueFactory.createLiteral(o));
    }

    public void addInt(Resource s, String p, int o) {
        this.add(s, p, this.valueFactory.createLiteral(o));
    }

    public void addInt(Resource s, IRI p, int o) {
        this.add(s, p, this.valueFactory.createLiteral(o));
    }

    public void add(Resource s, String p, Value o) {
        this.add(s, this.valueFactory.createIRI(p), o);
    }

    public void add(Resource s, IRI p, Value o) {
        this.collection.add(this.valueFactory.createStatement(
                s, p, o, this.defaultGraph));
    }

    public void addAll(File file) throws IOException {
        Optional<RDFFormat> format =
                Rio.getParserFormatForFileName(file.getName());
        if (!format.isPresent()) {
            throw new IOException("Can't get format for: " + file.getName());
        }
        this.addAll(file, format.get());
    }

    public void addAll(File file, RDFFormat format) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            this.addAll(stream, format);
        }
    }

    public void addAll(InputStream stream, RDFFormat format) throws IOException {
        try {
            RDFParser parser = Rio.createParser(format);
            parser.setRDFHandler(new StatementCollector(this.collection));
            parser.parse(stream, "http://localhost/base");
        } catch (RuntimeException ex) {
            throw new IOException(ex);
        }
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

    @Override
    public boolean add(Statement statement) {
        return this.collection.add(statement);
    }

    /**
     * Add statement and update the graph.
     */
    public boolean integrate(Statement statement) {
        Statement integratedStatement = this.valueFactory.createStatement(
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
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Statement> collection) {
        return this.collection.addAll(collection);
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

}
