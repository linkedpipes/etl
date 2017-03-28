package com.linkedpipes.plugin.transformer.valueParser;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

class ListProducer extends DefaultProducer {

    private final boolean addOrdering;

    private int counter = 0;

    private Resource lastResource = null;

    private IRI orderPredicate;

    public ListProducer(WritableSingleGraphDataUnit output,
            String predicate, boolean transferType, boolean addOrdering) {
        super(output, predicate, transferType);
        this.addOrdering = addOrdering;
        this.orderPredicate = valueFactory.createIRI(
                ValueParserVocabulary.HAS_ORDER);
    }

    @Override
    public void onEntityStart(Resource resource, Value value) {
        super.onEntityStart(resource, value);
        this.counter = 0;
        this.lastResource = null;
    }

    @Override
    public void onValue(String value) {
        Value rdfValue = createValue(value);
        if (lastResource == null) {
            addFirst(rdfValue);
        } else {
            addNext(rdfValue);
        }
        if (addOrdering) {
            addOrderingToLastResource();
        }
    }

    protected void addFirst(Value value) {
        lastResource = valueFactory.createBNode();

        this.buffer.add(valueFactory.createStatement(
                resource, predicate, lastResource));

        this.buffer.add(valueFactory.createStatement(
                lastResource, RDF.FIRST, value));
    }

    protected void addNext(Value value) {
        Resource nextResource = valueFactory.createBNode();

        this.buffer.add(valueFactory.createStatement(
                lastResource, RDF.REST, nextResource));

        this.buffer.add(valueFactory.createStatement(
                nextResource, RDF.FIRST, value));

        lastResource = nextResource;
    }

    protected void addOrderingToLastResource() {
        this.buffer.add(valueFactory.createStatement(
                lastResource, orderPredicate,
                valueFactory.createLiteral(++counter)));
    }


}
