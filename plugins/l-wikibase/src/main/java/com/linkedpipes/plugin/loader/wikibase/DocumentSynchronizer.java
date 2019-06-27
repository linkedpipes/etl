package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.implementation.PropertyIdValueImpl;
import org.wikidata.wdtk.datamodel.implementation.TermImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataEditor;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;
import java.util.Iterator;

class DocumentSynchronizer {

    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentSynchronizer.class);

    private final ExceptionFactory exceptionFactory;

    private final WikibaseDataEditor wbde;

    private final WikibaseDataFetcher wbdf;

    private WikibaseDocument expectedState;

    private ItemDocument document;

    public DocumentSynchronizer(
            ExceptionFactory exceptionFactory,
            ApiConnection connection,
            String siteIri) {
        this.exceptionFactory = exceptionFactory;
        this.wbde = new WikibaseDataEditor(connection, siteIri);
        this.wbdf = new WikibaseDataFetcher(connection, siteIri);
    }

    public void synchronize(WikibaseDocument expectedState)
            throws LpException, MediaWikiApiErrorException, IOException {
        this.expectedState = expectedState;
        document = getDocumentFromWikidata(expectedState);
        synchronizeLabels();
        for (WikibaseDocument.Statement st : expectedState.getStatements()) {
            synchronizeStatement(st);
        }
        saveDocument();
    }

    private ItemDocument getDocumentFromWikidata(
            WikibaseDocument expectedState)
            throws LpException, MediaWikiApiErrorException {
        if (expectedState.getQid() == null) {
            LOG.debug("New document created.");
            return ItemDocumentBuilder.forItemId(ItemIdValue.NULL).build();
        }
        EntityDocument document = wbdf.getEntityDocument(
                expectedState.getQid());
        if (document instanceof ItemDocument) {
            ItemDocument itemDocument = (ItemDocument) document;
            LOG.debug("Document: {} revision: {}",
                    itemDocument.getEntityId(),
                    document.getRevisionId());
            return (ItemDocument) document;
        } else {
            throw exceptionFactory.failure("Invalid document ({}) type: {}",
                    expectedState.getQid(), document.getClass().getName());
        }
    }

    private void synchronizeLabels() {
        expectedState.getLabels().forEach((key, value1) -> {
            MonolingualTextValue value =
                    new TermImpl(key, value1);
            document = document.withLabel(value);
        });
    }

    // https://wikibase.opendata.cz/prop/direct/P3 - > https://wikibase.opendata.cz/entity/P3
    private void synchronizeStatement(WikibaseDocument.Statement st) {
        Iterator<Statement> statements = document.getAllStatements();
        String stPredicate = getPredicateName(st.getPredicate());
        String stValue = "\"" + st.getValue() + "\"";
        while (statements.hasNext()) {
            Statement statement = statements.next();
            Snak snak = statement.getMainSnak();
            PropertyIdValue property = snak.getPropertyId();
            String propertyIri = getPredicateName(property.getIri());
            if (!propertyIri.equals(stPredicate)) {
                continue;
            }
            if (snak instanceof ValueSnak) {
                ValueSnak valueSnak = (ValueSnak) snak;
                String valueSnakStr = valueSnak.getValue().toString();
                if (valueSnakStr.equals(stValue)) {
                    // Statement is already set.
                    return;
                }
            }
        }
        createNewStatement(st);
    }

    private void createNewStatement(WikibaseDocument.Statement st) {
        LOG.debug("New statement: {} {}", st.getPredicate(), st.getValue());
        PropertyIdValue property = new PropertyIdValueImpl(
                getPredicateName(st.getPredicate()),
                st.getPredicate());

        Statement statement = StatementBuilder
                .forSubjectAndProperty(ItemIdValue.NULL, property)
                .withValue(Datamodel.makeStringValue(st.getValue())).build();

        document = document.withStatement(statement);
    }

    private String getPredicateName(String iri) {
        return iri.substring(iri.lastIndexOf("/") + 1);
    }

    private void saveDocument() throws IOException, MediaWikiApiErrorException {
        if (expectedState.getQid() == null) {
            document = wbde.createItemDocument(document, "Create new entity.");
        } else {
            document = wbde.editItemDocument(document, false, "Edit entity.");
        }
        LOG.debug("Saving document: {} revision: {}",
                document.getEntityId(),
                document.getRevisionId());
    }

}
