package com.linkedpipes.etl.storage.pipeline.transformation;

import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.rdf4j.Statements;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;

public class TransformationFacadeTest {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void importVisualisationPipeline() throws Exception {
        Statements input = new Statements(TestUtils.rdfFromResource(
                "pipeline/transformation/visualisationPipelineInput.trig"));
        Statements expected = new Statements(TestUtils.rdfFromResource(
                "pipeline/transformation/visualisationPipelineExpected.trig"));
        Statements options = Statements.ArrayList();

        Template sparqlEndpoint = Mockito.mock(Template.class);
        Mockito.when(sparqlEndpoint.getIri()).thenReturn(
                "http://etl.linkedpipes.com/resources/components/" +
                        "e-sparqlEndpoint/1.0.0");

        TemplateFacade templateFacade = Mockito.mock(TemplateFacade.class);
        Mockito.when(templateFacade.getTemplates()).thenReturn(
                Arrays.asList(sparqlEndpoint));
        Mockito.when(templateFacade.getTemplate(sparqlEndpoint.getIri()))
                .thenReturn(sparqlEndpoint);
        Mockito.when(templateFacade.getRootTemplate(sparqlEndpoint))
                .thenReturn(sparqlEndpoint);

        TransformationFacade transformation =
                new TransformationFacade(templateFacade, null);

        Collection<Statement> actual = transformation.localizeAndMigrate(
                input, options,
                this.valueFactory.createIRI("http://localhost/ppl"));

        Rdf4jUtils.rdfEqual(expected, actual);
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }

}

