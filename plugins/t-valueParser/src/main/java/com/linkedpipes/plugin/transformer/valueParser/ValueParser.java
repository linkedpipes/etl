package com.linkedpipes.plugin.transformer.valueParser;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValueParser implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public ValueParserConfiguration configuration;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() throws LpException {

        // Load input data.
        final List<Statement> statements = new ArrayList<>();
        final IRI source = valueFactory.createIRI(configuration.getSource());
        inputRdf.execute((connection -> {
            final RepositoryResult<Statement> result =
                    connection.getStatements(null, source, null,
                            inputRdf.getReadGraph());
            statements.clear();
            while (result.hasNext()) {
                statements.add(result.next());
            }
        }));
        // Parse.
        final Pattern pattern = Pattern.compile(configuration.getRegexp());
        final List<Statement> output = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            final String inputValue = statement.getObject().stringValue();
            final Matcher matcher = pattern.matcher(inputValue);
            while (matcher.find()) {
                configuration.getBindings().forEach((binding) -> {
                    final String value = matcher.group(binding.getGroup());
                    if (value == null) {
                        return;
                    }
                    // Create output.
                    output.add(valueFactory.createStatement(
                            statement.getSubject(),
                            valueFactory.createIRI(binding.getTarget()),
                            createValue(value, statement.getObject()),
                            outputRdf.getWriteGraph()));
                });
            }
        }
        // Add output.
        outputRdf.execute((connection) -> {
            connection.add(output);
        });
    }

    /**
     * Create value and optionally transfer metadata.
     *
     * @param value
     * @param originalValue
     * @return
     */
    private Value createValue(String value, Value originalValue) {
        if (configuration.isKeepMetadata()) {
            return valueFactory.createLiteral(value);
        }
        //
        if (originalValue instanceof Literal) {
            final Literal literal = (Literal) originalValue;
            if (literal.getDatatype() != null) {
                return valueFactory.createLiteral(value,
                        literal.getDatatype());
            } else if (literal.getLanguage().isPresent()) {
                return valueFactory.createLiteral(value,
                        literal.getLanguage().get());
            } else {
                return valueFactory.createLiteral(value);
            }
        } else {
            return valueFactory.createLiteral(value);
        }
    }

}
