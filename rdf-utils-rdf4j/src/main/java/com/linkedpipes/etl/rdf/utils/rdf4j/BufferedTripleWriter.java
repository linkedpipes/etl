package com.linkedpipes.etl.rdf.utils.rdf4j;

import com.linkedpipes.etl.rdf.utils.RdfSource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.util.Repositories;

import java.util.ArrayList;
import java.util.List;

class BufferedTripleWriter implements RdfSource.TypedTripleWriter<Value> {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    private final List<Statement> buffer = new ArrayList<>();

    private final String graph;

    private final Repository repository;

    public BufferedTripleWriter(String graph, Repository repository) {
        this.graph = graph;
        this.repository = repository;
    }

    @Override
    public void iri(String s, String p, String o) {
        buffer.add(VF.createStatement(VF.createIRI(s), VF.createIRI(p),
                VF.createIRI(o)));
    }

    @Override
    public void typed(String s, String p, String o, String type) {
        buffer.add(VF.createStatement(VF.createIRI(s), VF.createIRI(p),
                VF.createLiteral(o, VF.createIRI(type))));
    }

    @Override
    public void string(String s, String p, String o, String language) {
        if (language == null) {
            buffer.add(VF.createStatement(VF.createIRI(s), VF.createIRI(p),
                    VF.createLiteral(o)));
        } else {
            buffer.add(VF.createStatement(VF.createIRI(s), VF.createIRI(p),
                    VF.createLiteral(o, language)));
        }
    }

    @Override
    public void add(String s, String p, Value o) {
        buffer.add(VF.createStatement(VF.createIRI(s), VF.createIRI(p), o));
    }

    @Override
    public void submit() {
        Repositories.consume(repository, (connection) -> {
            connection.add(buffer, VF.createIRI(graph));
        });
    }

}
