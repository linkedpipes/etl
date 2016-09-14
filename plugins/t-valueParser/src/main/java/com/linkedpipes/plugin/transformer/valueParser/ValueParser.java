package com.linkedpipes.plugin.transformer.valueParser;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Škoda Petr
 */
public final class ValueParser implements Component.Sequential {

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.InputPort(id = "OutputRdf")
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
                            inputRdf.getGraph());
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
                            outputRdf.getGraph()));
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
