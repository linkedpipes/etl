package com.linkedpipes.plugin.transformer.valueParser;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValueParser implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public ValueParserConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private List<Statement> statements;

    private Map<String, DefaultProducer> producers = new HashMap<>();

    private Pattern pattern;

    @Override
    public void execute() throws LpException {
        prepareRegExpPattern();
        prepareProducers();
        loadStatementsToParse();
        //
        parseStatements();
    }

    private void prepareRegExpPattern() {
        pattern = Pattern.compile(configuration.getRegexp());
    }

    private List<Statement> loadStatementsToParse() throws LpException {
        statements = new ArrayList<>();
        IRI source = valueFactory.createIRI(configuration.getSource());
        inputRdf.execute((connection -> {
            RepositoryResult<Statement> result = connection.getStatements(
                    null, source, null, inputRdf.getReadGraph());
            statements.clear();
            while (result.hasNext()) {
                statements.add(result.next());
            }
        }));
        return statements;
    }

    private void prepareProducers() throws LpException {
        for (ValueParserConfiguration.OutputBinding output
                : configuration.getBindings()) {
            producers.put(output.getGroup(),
                    createProducer(output.getTarget(), output.getType()));
        }
    }

    private DefaultProducer createProducer(String predicate, String type)
            throws LpException {
        // For backward compatibility.
        if (type == null) {
            type = ValueParserVocabulary.HAS_DEFAULT;
        }
        switch (type) {
            case ValueParserVocabulary.LIST_WITH_INDEX:
                return new ListProducer(outputRdf, predicate,
                        configuration.isKeepMetadata(),
                        true);
            case ValueParserVocabulary.HAS_DEFAULT:
                return new DefaultProducer(outputRdf, predicate,
                        configuration.isKeepMetadata());
            default:
                throw exceptionFactory.failure("Invalid type: {}", type);
        }
    }

    private void parseStatements() throws LpException {
        for (Statement statement : statements) {
            onBeforeStatement(statement.getSubject(), statement.getObject());
            parseValue(statement.getObject().stringValue());
            onAfterStatement();
        }
    }

    private void onBeforeStatement(Resource resource, Value value) {
        for (DefaultProducer producer : producers.values()) {
            producer.onEntityStart(resource, value);
        }
    }

    private void parseValue(String value) {
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            for (Map.Entry<String, DefaultProducer> entry
                    : producers.entrySet()) {
                String groupValue = matcher.group(entry.getKey());
                if (groupValue == null) {
                    continue;
                }
                entry.getValue().onValue(groupValue);
            }
        }
    }

    private void onAfterStatement() throws LpException {
        for (DefaultProducer producer : producers.values()) {
            producer.onEntityEnd();
        }
    }

}
