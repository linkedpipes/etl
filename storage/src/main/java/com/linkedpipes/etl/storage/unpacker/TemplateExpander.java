package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.ClosableRdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.unpacker.model.GraphCollection;
import com.linkedpipes.etl.storage.unpacker.model.ModelLoader;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerComponent;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorComponent;
import com.linkedpipes.etl.storage.unpacker.model.template.JarTemplate;
import com.linkedpipes.etl.storage.unpacker.model.template.ReferenceTemplate;
import com.linkedpipes.etl.storage.unpacker.model.template.Template;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

class TemplateExpander {

    private final TemplateSource templateSource;

    private final JarExpander jarExpander;

    private final ReferenceExpander referenceExpander;

    public TemplateExpander(TemplateSource templateSource) {
        this.templateSource = templateSource;
        jarExpander = new JarExpander(templateSource);
        referenceExpander = new ReferenceExpander(templateSource, this);
    }

    public void setGraphs(GraphCollection graphs) {
        jarExpander.setGraphs(graphs);
        referenceExpander.setGraphs(graphs);
    }

    public ExecutorComponent expand(DesignerComponent srcComponent)
            throws BaseException {

        Template template = getTemplate(srcComponent);
        if (template instanceof JarTemplate) {
            return expandJarTemplate(srcComponent, (JarTemplate) template);
        } else if (template instanceof ReferenceTemplate) {
            return expandReferenceTemplate(srcComponent,
                    (ReferenceTemplate) template);
        } else {
            throw new BaseException("Invalid template type: {}",
                    template.getClass().getName());
        }
    }

    private Template getTemplate(DesignerComponent component)
            throws BaseException {
        String templateIri = component.getTemplate();
        Collection<Statement> definition =
                templateSource.getDefinition(templateIri);
        return loadTemplate(definition);
    }

    private Template loadTemplate(Collection<Statement> templateAsRdf)
            throws BaseException {
        ClosableRdfSource source = Rdf4jSource.wrapInMemory(templateAsRdf);
        try {
            return ModelLoader.loadTemplate(source);
        } catch (RdfUtilsException ex) {
            throw new BaseException("Can't load object.", ex);
        } finally {
            source.close();
        }
    }

    private ExecutorComponent expandJarTemplate(
            DesignerComponent srcComponent, JarTemplate template)
            throws BaseException {
        ExecutorComponent component = jarExpander.expand(
                srcComponent.getIri(),
                srcComponent.getConfigurationGraphs(),
                template);
        copyBasicInformation(srcComponent, component);
        return component;
    }

    private void copyBasicInformation(
            DesignerComponent sourceComponent,
            ExecutorComponent targetComponent) {
        targetComponent.setLabel(sourceComponent.getLabel());
    }

    private ExecutorComponent expandReferenceTemplate(
            DesignerComponent srcComponent, ReferenceTemplate template)
            throws BaseException {
        ExecutorComponent component = referenceExpander.expand(
                srcComponent, template);
        copyBasicInformation(srcComponent, component);
        return component;
    }

}
