package com.linkedpipes.plugin.transformer.valueParser;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

class DefaultProducer extends ValueProducer {

    protected boolean transferType;

    protected Value originalValue;

    public DefaultProducer(WritableSingleGraphDataUnit output,
            String predicate, boolean transferType) {
        super(output, predicate);
        this.transferType = transferType;
    }

    @Override
    public void onEntityStart(Resource resource, Value value) {
        super.onEntityStart(resource, value);
        this.originalValue = value;
    }

    @Override
    public void onValue(String value) {
        Value rdfValue = createValue(value);
        this.buffer.add(valueFactory.createStatement(
                resource, predicate, rdfValue));
    }

    protected Value createValue(String value) {
        if (transferType) {
            return createPreserveTypeAndLanguage(value);
        } else {
            return createPureStringValue(value);
        }
    }

    protected Value createPureStringValue(String value) {
        return valueFactory.createLiteral(value);
    }

    protected Value createPreserveTypeAndLanguage(String value) {
        if (originalValue instanceof Literal) {
            Literal literal = (Literal)originalValue;
            if (literal.getLanguage().isPresent()) {
                return valueFactory.createLiteral(value,
                        literal.getLanguage().get());
            } else  if (literal.getDatatype() != null) {
                return valueFactory.createLiteral(value,
                        literal.getDatatype());
            } else {
                return createPureStringValue(value);
            }
        } else {
            return createPureStringValue(value);
        }
    }

}
