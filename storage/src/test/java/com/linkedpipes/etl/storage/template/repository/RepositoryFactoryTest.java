package com.linkedpipes.etl.storage.template.repository;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RepositoryFactoryTest {

    @Test
    public void migrate() throws StorageException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        List<PluginTemplate> plugins = new ArrayList<>();
        plugins.add(new PluginTemplate(
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/resources/components/" +
                                "e-textHolder/0.0.0"),
                null, null, null, false, null, null,
                null, null, null, null, null, null));

        TemplateRepositoryFactory factory = new TemplateRepositoryFactory(plugins);
        var actual = factory.create(new File("d:/Temp/20221011"));
        var pluginTemplates = actual.listPluginTemplates();
        var referenceTemplates = actual.listReferenceTemplates();
        return;
    }

}
