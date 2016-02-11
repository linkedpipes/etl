package com.linkedpipes.plugin.transformer.tabular;

import java.util.List;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 *
 * @author Petr Škoda
 */
class UrlTemplate {

    protected static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    private final StringTemplate template;

    public UrlTemplate(String templateAsString) {
        this.template = new StringTemplate(templateAsString);
    }

    public void initialize(String tableUri, List<String> header) throws InvalidTemplate {
        template.initialize(tableUri, header);
    }

    public IRI getUrl(List<String> row, int rowNumber) {
        final String value = template.process(row);
        if (value == null) {
            return null;
        } else {
            return VALUE_FACTORY.createIRI(value);
        }
    }

}
