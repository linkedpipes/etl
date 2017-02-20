package com.linkedpipes.plugin.transformer.tabularuv.mapper;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.plugin.transformer.tabularuv.RdfWriter;
import com.linkedpipes.plugin.transformer.tabularuv.TabularOntology;
import com.linkedpipes.plugin.transformer.tabularuv.column.ValueGenerator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parse table data into RDF. Before usage this class must be configured by
 * {@link TableToRdfConfigurator}.
 */
public class TableToRdf {

    private static final Logger LOG = LoggerFactory.getLogger(TableToRdf.class);

    final ValueFactory valueFactory;

    final TableToRdfConfig config;

    ValueGenerator[] infoMap = null;

    ValueGenerator keyColumn = null;

    String baseUri = null;

    Map<String, Integer> nameToIndex = null;

    IRI rowClass = null;

    private final IRI typeUri;

    IRI tableSubject = null;

    boolean tableInfoGenerated = false;

    final RdfWriter outRdf;

    public TableToRdf(TableToRdfConfig config, RdfWriter writer,
            ValueFactory valueFactory) {
        this.config = config;
        this.outRdf = writer;
        this.valueFactory = valueFactory;
        this.typeUri = valueFactory.createIRI(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    }

    public void paserRow(List<Object> row, int rowNumber) throws LpException {
        if (row.size() < nameToIndex.size()) {
            LOG.warn("Row is smaller ({} instead of {}) - ignore.",
                    row.size(), nameToIndex.size());
            return;
        } else if (row.size() > nameToIndex.size()) {
            LOG.warn("Row is too big, some data may be invalid!"
                            + " (size: {} expected: {})", row.size(),
                    nameToIndex.size());
        }
        // trim string values
        if (config.trimString) {
            List<Object> newRow = new ArrayList<>(row.size());
            for (Object item : row) {
                if (item instanceof String) {
                    final String itemAsString = (String) item;
                    newRow.add(itemAsString.trim());
                } else {
                    newRow.add(item);
                }
            }
            row = newRow;
        }
        // get subject - key
        final IRI subj = prepareUri(row, rowNumber);
        if (subj == null) {
            LOG.error("Row ({}) has null key, row skipped.", rowNumber);
        }
        // parse the line, based on configuration
        for (ValueGenerator item : infoMap) {
            final IRI predicate = item.getUri();
            final Value value = item.generateValue(row, valueFactory);
            if (value == null) {
                if (config.ignoreBlankCells) {
                    // ignore
                } else {
                    // insert blank cell IRI
                    outRdf.add(subj, predicate, TabularOntology.BLANK_CELL);
                }
            } else {
                // insert value
                outRdf.add(subj, predicate, value);
            }
        }
        // add row data - number, class, connection to table
        if (config.generateRowTriple) {
            outRdf.add(subj, TabularOntology.ROW_NUMBER,
                    valueFactory.createLiteral(rowNumber));
        }
        if (rowClass != null) {
            outRdf.add(subj, typeUri, rowClass);
        }
        if (tableSubject != null) {
            outRdf.add(tableSubject, TabularOntology.TABLE_HAS_ROW, subj);
        }
        // Add table statistict only for the first time.
        if (!tableInfoGenerated && tableSubject != null) {
            tableInfoGenerated = true;
            if (config.generateTableClass) {
                outRdf.add(tableSubject, RDF.TYPE, TabularOntology.TABLE_CLASS);
            }
        }
    }

    /**
     * Set subject that will be used as table subject.
     *
     * @param newTableSubject Null to turn this functionality off.
     */
    public void setTableSubject(IRI newTableSubject) {
        tableSubject = newTableSubject;
        tableInfoGenerated = false;
    }

    /**
     * Return key for given row.
     *
     * @param row
     * @param rowNumber
     * @return
     */
    protected IRI prepareUri(List<Object> row, int rowNumber) {
        if (keyColumn == null) {
            return valueFactory
                    .createIRI(baseUri + Integer.toString(rowNumber));
        } else {
            return (IRI) keyColumn.generateValue(row, valueFactory);
        }
    }

}
