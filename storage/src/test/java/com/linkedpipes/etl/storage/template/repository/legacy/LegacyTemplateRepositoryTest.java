package com.linkedpipes.etl.storage.template.repository.legacy;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.TestUtils;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LegacyTemplateRepositoryTest {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void migrateRepository() throws StorageException {
        List<PluginTemplate> plugins = new ArrayList<>();
        plugins.add(new PluginTemplate(
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/resources/components/" +
                                "e-textHolder/0.0.0"),
                null, null, null, true,
                Collections.emptyList(), null, Collections.emptyMap(),
                Collections.emptyList(),
                null, null, null, null));
        //
        LegacyTemplateRepository repository =
                new LegacyTemplateRepository(plugins, 4);
        File root = TestUtils.file("template/repository/legacy");
        var actual = repository.loadReferenceTemplates(
                new File(root, "templates"),
                new File(root, "knowledge"));
        Assertions.assertEquals(1, actual.size());
        var component = actual.get(0);
        Assertions.assertEquals(
                "http://localhost:8080/resources/components/" +
                        "1622805775578-ff2aa41d-16d7-413f-b44b-99b8902a76ad",
                component.resource().stringValue());
        Assertions.assertEquals(5, component.version());
        Assertions.assertEquals(
                "http://etl.linkedpipes.com/resources/components/" +
                        "e-textHolder/0.0.0",
                component.plugin().stringValue());
    }

}
