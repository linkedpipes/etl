package com.linkedpipes.etl.library.template.plugin;

import com.linkedpipes.etl.library.template.TestUtils;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class PluginTemplateFacadeTest {

    @Test
    public void loadTextHolderV1() throws Exception {
        File file = TestUtils.fileFromResource("plugin/e-textHolder-v1.jar");
        JavaPlugin plugin = PluginTemplateFacade.loadJavaFile(file);

        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/jars/e-textHolder/0.0.0",
                plugin.iri().stringValue());

        Assertions.assertEquals(1, plugin.templates().size());

        PluginTemplate template = plugin.templates().get(0);

        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/components/" +
                        "e-textHolder/0.0.0",
                template.resource().stringValue());
        Assertions.assertEquals(
                3, template.dialogs().size());
        var dialogs = template.dialogs();
        Assertions.assertTrue(dialogs.containsKey("config"));
        Assertions.assertTrue(dialogs.containsKey("template"));
        Assertions.assertTrue(dialogs.containsKey("instance"));
        // Try to copy out a file.
        String fullEntryName = dialogs.get("instance").get("dialog.html");
        String actualFileContent = new String(PluginTemplateFacade.readFile(
                plugin, plugin.entry(fullEntryName)));
        Assertions.assertEquals(
                "<lp-dialog-control-instance "
                        + "lp-dialog=\"dialog\" "
                        + "lp-application=\"application\">"
                        + "</lp-dialog-control-instance>\n",
                actualFileContent);
    }

    @Test
    public void loadTextHolderV2() throws Exception {
        File file = TestUtils.fileFromResource("plugin/e-textHolder-v2.jar");
        JavaPlugin plugin = PluginTemplateFacade.loadJavaFile(file);

        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/jars/e-textHolder",
                plugin.iri().stringValue());

        Assertions.assertEquals(1, plugin.templates().size());

        PluginTemplate template = plugin.templates().get(0);

        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/components/" +
                        "e-textHolder/0.0.0",
                template.resource().stringValue());
        Assertions.assertEquals(
                3, template.dialogs().size());
        var dialogs = template.dialogs();
        Assertions.assertTrue(dialogs.containsKey("config"));
        Assertions.assertTrue(dialogs.containsKey("template"));
        Assertions.assertTrue(dialogs.containsKey("instance"));
        // Try to copy out a file.
        String fullEntryName = dialogs.get("instance").get("dialog.html");
        String actualFileContent = new String(PluginTemplateFacade.readFile(
                plugin, plugin.entry(fullEntryName)));
        Assertions.assertEquals(
                "<lp-dialog-control-instance "
                        + "lp-dialog=\"dialog\" "
                        + "lp-application=\"application\">"
                        + "</lp-dialog-control-instance>\n",
                actualFileContent);
    }

}
