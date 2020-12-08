package cz.skodape.hdt.rdf.rdf4j;

import cz.skodape.hdt.core.MemoryReferenceSource;
import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.Reference;
import cz.skodape.hdt.core.ReferenceSource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Rdf4jMemorySource extends Rdf4jSource {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Rdf4jMemorySourceConfiguration configuration;

    private Model model = null;

    public Rdf4jMemorySource(Rdf4jMemorySourceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void open() throws OperationFailed {
        File inputFile = this.configuration.file;
        Optional<RDFFormat> format = Rio.getParserFormatForFileName(
                inputFile.getName());
        if (format.isEmpty()) {
            throw new OperationFailed("Can't determine file format.");
        }
        try (InputStream stream = new FileInputStream(inputFile)) {
            this.model = Rio.parse(stream, "http://localhost/", format.get());
        } catch (IOException ex) {
            throw new OperationFailed("Can't read file.", ex);
        }
    }

    @Override
    public void close() {
        this.model.clear();
    }

    @Override
    public ReferenceSource roots() {
        Set<ResourceInGraph> subjects = new HashSet<>();
        model.stream().map(ResourceInGraph::new).forEach(subjects::add);
        List<Reference> references = subjects.stream()
                .map(item -> (Reference) this.wrap(item.graph, item.resource))
                .collect(Collectors.toList());
        return new MemoryReferenceSource<>(references);
    }

    @Override
    protected List<Value> property(
            Resource graph, Resource resource, String property) {
        IRI predicate = valueFactory.createIRI(property);
        List<Value> result = new ArrayList<>();
        Iterable<Statement> statements;
        if (this.configuration.graphAware) {
            statements = model.getStatements(resource, predicate, null, graph);
        } else {
            statements = model.getStatements(resource, predicate, null);
        }
        for (var statement : statements) {
            result.add(statement.getObject());
        }
        return result;
    }

}
