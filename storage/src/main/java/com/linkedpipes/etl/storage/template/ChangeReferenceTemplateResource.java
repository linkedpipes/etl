package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.configuration.ConfigurationFacade;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;

public class ChangeReferenceTemplateResource {

    public ReferenceTemplate localize(
            PluginTemplate plugin,
            ReferenceTemplate reference,
            Resource resource) {
        return new ReferenceTemplate(
                resource, reference.version(), reference.template(),
                reference.plugin(), reference.label(), reference.description(),
                reference.note(), reference.color(), reference.tags(),
                reference.knownAs(),
                ConfigurationFacade.localizeConfiguration(
                        plugin.configurationDescription(),
                        reference.configuration().selector(),
                        resource),
                ConfigurationFacade.configurationGraph(resource));
    }

}
