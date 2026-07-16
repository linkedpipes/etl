package com.linkedpipes.etl.unpacker.template;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.unpacker.TemplateSource;
import com.linkedpipes.etl.unpacker.UnpackerException;
import com.linkedpipes.etl.unpacker.model.ModelLoader;
import com.linkedpipes.etl.unpacker.model.template.Template;
import java.util.Collection;
import org.eclipse.rdf4j.model.Statement;

/**
 * Resolves component templates from statements already loaded from the
 * pipeline file itself. The file is expected to be self-contained: for every
 * referenced template, it carries the template's definition (in a graph
 * named after the template's own IRI), its default configuration, and its
 * configuration description (in the graphs the definition points to). This
 * lets executor unpack a pipeline without any runtime dependency on storage.
 */
public class GraphTemplateSource implements TemplateSource {

    private final StatementsSelector statements;

    public GraphTemplateSource(StatementsSelector statements) {
        this.statements = statements;
    }

    @Override
    public Collection<Statement> getDefinition(String iri) throws UnpackerException {
        return selectGraph(iri);
    }

    @Override
    public Collection<Statement> getConfiguration(String iri) throws UnpackerException {
        Template template = loadTemplate(iri);
        return selectGraph(template.getConfigGraph());
    }

    @Override
    public Collection<Statement> getConfigurationDescription(String iri) throws UnpackerException {
        Template template = loadTemplate(iri);
        return selectGraph(template.getConfigDescriptionGraph());
    }

    private Template loadTemplate(String iri) throws UnpackerException {
        Collection<Statement> definition = selectGraph(iri);
        return ModelLoader.loadTemplate(Statements.wrap(definition));
    }

    private Collection<Statement> selectGraph(String graph) throws UnpackerException {
        Collection<Statement> result = statements.selectByGraph(graph);
        if (result.isEmpty()) {
            throw new UnpackerException("Missing required template graph '{}'.", graph);
        }
        return result;
    }
}
