package com.linkedpipes.etl.storage.unpacker.model.template;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.ClosableRdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.storage.unpacker.model.ModelLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TemplateJarTest {

    @Test
    public void load_textHolder() throws Exception {
        Template template = loadTemplate(
                "unpacker/template/definition/e-textHolder.trig");
        if (!(template instanceof JarTemplate)) {
            Assert.fail("Invalid template type.");
        }

        JarTemplate jarTemplate = (JarTemplate)template;

        Assert.assertEquals(
                "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0",
                jarTemplate.getIri());
        Assert.assertEquals(1, jarTemplate.getTypes().size());
        Assert.assertEquals(
                "http://etl.linkedpipes.com/resources/jars/e-textHolder/0.0.0",
                jarTemplate.getJar());
        Assert.assertEquals(1, jarTemplate.getRequirements().size());
        Assert.assertEquals(
                "http://linkedpipes.com/resources/requirement/workingDirectory",
                jarTemplate.getRequirements().get(0));
        Assert.assertEquals(1, jarTemplate.getPorts().size());
        Assert.assertEquals(
                "http://linkedpipes.com/resources/components/e-textHolder/0.0.0/configuration/desc",
                jarTemplate.getConfigDescriptionGraph());
        Assert.assertTrue(jarTemplate.isSupportControl());

        TemplatePort port = jarTemplate.getPorts().get(0);
        Assert.assertEquals(1, port.getRequirements().size());
        Assert.assertEquals(
                "http://linkedpipes.com/resources/requirement/workingDirectory",
                port.getRequirements().get(0));
        Assert.assertEquals(2, port.getTypes().size());
        Assert.assertTrue(port.getTypes().contains(
                "http://linkedpipes.com/ontology/Output"));
        Assert.assertTrue(port.getTypes().contains(
                "http://linkedpipes.com/ontology/dataUnit/system/1.0/files/DirectoryMirror"));
        Assert.assertEquals("FilesOutput", port.getBinding());
    }

    private Template loadTemplate(String resourceName)
            throws IOException, RdfUtilsException {
        ClosableRdfSource source = Rdf4jUtils.loadAsSource(resourceName);
        Template template = ModelLoader.loadTemplate(source);
        source.close();
        return template;
    }

}
