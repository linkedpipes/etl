package com.linkedpipes.etl.library.pipeline.migration;

import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import com.linkedpipes.etl.library.pipeline.adapter.RawPipelineComponent;
import org.eclipse.rdf4j.model.Resource;

import java.util.Set;


/**
 * Change core templates from local host to etl.linkedpipes.com.
 *
 * <p>Example of conversion:
 * http://localhost:8080/resources/components/t-tabular
 * http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0
 */
public class PipelineV0 {

    private final Set<Resource> plugins;

    private final boolean throwOnMissing;

    public PipelineV0(Set<Resource> plugins, boolean throwOnMissing) {
        this.plugins = plugins;
        this.throwOnMissing = throwOnMissing;
    }

    public void migrateToV1(RawPipeline pipeline) throws PipelineMigrationFailed {
        for (RawPipelineComponent component : pipeline.components) {
            migrateComponent(component);
        }
    }

    private void migrateComponent(RawPipelineComponent component)
            throws PipelineMigrationFailed {
        String name = templateName(component.template);
        Resource newTemplateIri = searchMatchingTemplateByName(name);
        if (newTemplateIri == null) {
            if (throwOnMissing) {
                throw new PipelineMigrationFailed(
                        "Can not convert '{}' for '{}'",
                        name, component.resource);
            }
        }
        component.template = newTemplateIri;
    }

    private String templateName(Resource template) {
        // The extracted name is /t-tabular, and we add / to the end
        // to prevent t-tabular to match t-tabularUv, also every name
        // is followed by /{version}.
        String iri = template.stringValue();
        return iri.substring(iri.lastIndexOf("/")) + "/";
    }

    private Resource searchMatchingTemplateByName(String name) {
        for (Resource plugin : plugins) {
            if (plugin.stringValue().contains(name)) {
                return plugin;
            }
        }
        return null;
    }

}
