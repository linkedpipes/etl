package com.linkedpipes.etl.storage.http.model;

import com.linkedpipes.etl.storage.distribution.ImportPipeline;
import com.linkedpipes.etl.storage.distribution.ImportTemplate;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.List;

public record ImportResponse(
        List<Pipeline> pipelines,
        List<ReferenceTemplate> referenceTemplates
) {

    public record Pipeline(
            /*
             * Original pipeline resource.
             */
            Resource original,
            /*
             * Router of imported pipeline. In null import has failed.
             */
            Resource local,
            /*
             * Label.
             */
            String label,
            /*
             * Tags.
             */
            List<String> tags,
            /*
             * Optional exception related to import operation.
             */
            Exception exception,
            /*
             * True if pipeline was saved.
             */
            boolean stored
    ) {

    }

    public record ReferenceTemplate(
            /*
             * Original reference template resource.
             */
            Resource original,
            /*
             * Router of imported template or its local counterpart.
             */
            Resource local,
            /*
             * Label.
             */
            String label,
            /*
             * Tags.
             */
            List<String> tags,
            /*
             * Optional exception related to import operation.
             */
            Exception exception,
            /*
             * True if template was stored as new.
             */
            boolean storedAsNew,
            /*
             * Stored as existing, i.e. template update.
             */
            boolean storedAsExisting
    ) {

        public boolean stored() {
            return storedAsNew || storedAsExisting;
        }

    }

    public static ImportResponse create(
            ImportTemplate importTemplate, ImportPipeline importPipeline) {
        List<ReferenceTemplate> templates = new ArrayList<>();
        for (ImportTemplate.Container container :
                importTemplate.getContainers()) {
            Resource localResource;
            if (container.local() != null) {
                localResource = container.local().resource();
            } else {
                localResource = importTemplate.getRemoteToLocal().get(
                        container.raw().resource);
            }
            templates.add(new ReferenceTemplate(
                    container.raw().resource,
                    localResource,
                    container.raw().label,
                    container.raw().tags,
                    container.exception(),
                    container.storedAsNew(),
                    container.storedAsExisting()));
        }
        List<Pipeline> pipelines = new ArrayList<>();
        for (ImportPipeline.Container container :
                importPipeline.getContainers()) {
            Resource localResource = null;
            if (container.local() != null) {
                localResource = container.local().resource();
            }
            pipelines.add(new Pipeline(
                    container.raw().resource,
                    localResource,
                    container.raw().label,
                    container.raw().tags,
                    container.exception(),
                    container.stored()));
        }
        return new ImportResponse(pipelines, templates);
    }

}
