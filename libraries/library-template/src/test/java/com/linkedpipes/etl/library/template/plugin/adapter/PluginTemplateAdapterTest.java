package com.linkedpipes.etl.library.template.plugin.adapter;

import com.linkedpipes.etl.library.template.TestUtils;
import com.linkedpipes.etl.library.template.plugin.PluginTemplateFacade;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class PluginTemplateAdapterTest {

    @Test
    public void toRdfAndBackV2() throws Exception{
        File file = TestUtils.file("plugin/e-textHolder-v2.jar");
        JavaPlugin plugin = PluginTemplateFacade.loadJavaFile(file);

        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/jars/e-textHolder",
                plugin.iri().stringValue());

        Assertions.assertEquals(1, plugin.templates().size());
        PluginTemplate expected = plugin.templates().get(0);

        var statements = PluginTemplateToRdf.asRdf(expected);
        List<PluginTemplate> candidates = RdfToPluginTemplate.asPluginTemplates(
                statements.selector());
        Assertions.assertEquals(1, candidates.size());

        // We do not load the dialog data.
        var expectedWithoutDialogs = new PluginTemplate(
                expected.resource(), expected.label(), expected.color(),
                expected.type(), expected.supportControl(),
                expected.tags(), expected.documentation(),
                null, expected.ports(),
                expected.configuration(),
                expected.configurationGraph(),
                expected.configurationDescription(),
                expected.configurationDescriptionGraph());

        var actual = candidates.get(0);
        var actualWithoutDialogs = new PluginTemplate(
                actual.resource(), actual.label(), actual.color(),
                actual.type(), actual.supportControl(),
                actual.tags(), actual.documentation(),
                null, actual.ports(),
                actual.configuration(),
                actual.configurationGraph(),
                actual.configurationDescription(),
                actual.configurationDescriptionGraph());

        Assertions.assertEquals(expectedWithoutDialogs, actualWithoutDialogs);
    }

}
