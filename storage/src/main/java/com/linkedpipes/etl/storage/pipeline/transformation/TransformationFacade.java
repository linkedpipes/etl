package com.linkedpipes.etl.storage.pipeline.transformation;

import com.linkedpipes.etl.storage.template.mapping.MappingFacade;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.pipeline.PipelineInfo;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class TransformationFacade {

    private final TemplateFacade templateFacade;

    private final MappingFacade mappingFacade;

    @Autowired
    public TransformationFacade(
            TemplateFacade templates, MappingFacade mappings) {
        this.templateFacade = templates;
        this.mappingFacade = mappings;
    }

    /**
     * Used for loading pipelines.
     */
    public Collection<Statement> migrateThrowOnWarning(
            Collection<Statement> pipeline)
            throws TransformationFailed {
        Migration migration = new Migration(templateFacade);
        migration.setThrowOnWarning(true);
        return migration.migrate(pipeline);
    }

    /**
     * Used to import a pipeline.
     */
    public Collection<Statement> localizeAndMigrate(
            Collection<Statement> pipeline,
            Collection<Statement> options,
            IRI newIri) throws TransformationFailed {
        PipelineInfo info = loadPipelineInfo(pipeline);

        // Localization may import templates, which may needed to be localized
        // as well, this is done in the transformer.
        ImportTransformer transformer = new ImportTransformer(
                templateFacade, mappingFacade);
        pipeline = transformer.localizePipeline(
                pipeline, options, info, newIri);

        if (info.getVersion() != Pipeline.VERSION_NUMBER) {
            Migration migration = new Migration(templateFacade);
            migration.setThrowOnWarning(false);
            pipeline = migration.migrate(pipeline);
        }

        return pipeline;
    }

    private PipelineInfo loadPipelineInfo(Collection<Statement> pipeline)
            throws TransformationFailed {
        PipelineInfo info = new PipelineInfo();
        try {
            PojoLoader.loadOfType(pipeline, Pipeline.TYPE, info);
        } catch (PojoLoader.CantLoadException ex) {
            throw new TransformationFailed(
                    "Can't load pipeline info.", ex);
        }
        return info;
    }

}
