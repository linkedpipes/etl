package com.linkedpipes.etl.unpacker.model.template;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import com.linkedpipes.etl.unpacker.model.ModelLoader;
import com.linkedpipes.etl.unpacker.model.template.JarTemplate;
import com.linkedpipes.etl.unpacker.model.template.Template;
import com.linkedpipes.etl.unpacker.model.template.TemplatePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TemplateJarTest {

    @Test
    public void load_textHolder() throws Exception {
        Template template = loadTemplate(
                "unpacker/template/definition/e-textHolder.trig");
        if (!(template instanceof JarTemplate)) {
            Assertions.fail("Invalid template type.");
        }

        JarTemplate jarTemplate = (JarTemplate) template;

        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0",
                jarTemplate.getIri());
        Assertions.assertEquals(1, jarTemplate.getTypes().size());
        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/jars/e-textHolder/0.0.0",
                jarTemplate.getJar());
        Assertions.assertEquals(1, jarTemplate.getRequirements().size());
        Assertions.assertEquals(
                "http://linkedpipes.com/resources/requirement/workingDirectory",
                jarTemplate.getRequirements().get(0));
        Assertions.assertEquals(1, jarTemplate.getPorts().size());
        Assertions.assertEquals(
                "http://linkedpipes.com/resources/components/e-textHolder/0.0.0/configuration/desc",
                jarTemplate.getConfigDescriptionGraph());
        Assertions.assertTrue(jarTemplate.isSupportControl());

        TemplatePort port = jarTemplate.getPorts().get(0);
        Assertions.assertEquals(1, port.getRequirements().size());
        Assertions.assertEquals(
                "http://linkedpipes.com/resources/requirement/workingDirectory",
                port.getRequirements().get(0));
        Assertions.assertEquals(2, port.getTypes().size());
        Assertions.assertTrue(port.getTypes().contains(
                "http://linkedpipes.com/ontology/Output"));
        Assertions.assertTrue(port.getTypes().contains(
                "http://linkedpipes.com/ontology/dataUnit/system/1.0/files/DirectoryMirror"));
        Assertions.assertEquals("FilesOutput", port.getBinding());
    }

    private Template loadTemplate(String resourceName)
            throws StorageException {
        Statements statements;
        try {
            statements = TestUtils.statements(resourceName);
        } catch (IOException ex) {
            throw new StorageException("Can't load statements", ex);
        }
        return ModelLoader.loadTemplate(statements.selector());
    }

}
