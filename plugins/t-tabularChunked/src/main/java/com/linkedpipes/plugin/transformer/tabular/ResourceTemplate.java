package com.linkedpipes.plugin.transformer.tabular;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.List;

/**
 * Used to create resources.
 */
public class ResourceTemplate {

    protected static final ValueFactory VALUE_FACTORY
            = SimpleValueFactory.getInstance();

    private final StringTemplate template;

    /**
     * Last created resource.
     */
    private Resource resource;

    private int lastRowNumber;

    public ResourceTemplate(String templateAsString) {
        if (templateAsString == null || templateAsString.isEmpty()) {
            template = null;
        } else {
            this.template = new StringTemplate(templateAsString);
        }
    }

    public void initialize(String tableUri, List<String> header)
            throws InvalidTemplate {
        lastRowNumber = -1;
        if (template != null) {
            template.initialize(tableUri, header);
        }
    }

    public Resource getResource(List<String> row, int rowNumber) {
        // Return same value on the same row.
        if (rowNumber == lastRowNumber) {
            return resource;
        } else {
            lastRowNumber = rowNumber;
            if (template == null) {
                resource = VALUE_FACTORY.createBNode();
            } else {
                final String value = template.process(row);
                if (value == null) {
                    return null;
                } else {
                    resource = VALUE_FACTORY.createIRI(value);
                }
            }
            return resource;
        }
    }

}
