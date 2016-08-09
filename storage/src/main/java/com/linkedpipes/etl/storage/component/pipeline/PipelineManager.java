package com.linkedpipes.etl.storage.component.pipeline;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.migration.MigrationFacade;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.linkedpipes.etl.storage.rdf.RdfUtils.write;

/**
 *
 * @author Petr Škoda
 */
@Service
class PipelineManager {

    private static final Logger LOG
            = LoggerFactory.getLogger(PipelineManager.class);

    @Autowired
    private Configuration configuration;

    private final Map<String, Pipeline> pipelines = new HashMap<>();

    @PostConstruct
    public void initialize() {
        final File pipelineDirectory = configuration.getPipelinesDirectory();
        if (!pipelineDirectory.exists()) {
            pipelineDirectory.mkdirs();
        }
        for (File item : pipelineDirectory.listFiles()) {
            if (!item.isFile()) {
                continue;
            }
            if (item.getName().toLowerCase().endsWith(".backup")) {
                // Ignore back up files.
                continue;
            }
            // Check for migration from JSON version.
            if (item.getName().toLowerCase().endsWith(".json")) {
                item = MigrationFacade.migrateJsonPipeline(item);
                if (item == null) {
                    continue;
                }
            }
            // Load the pipeline.
            try {
                final Pipeline pipeline = loadPipeline(item);
                pipelines.put(pipeline.getIri(), pipeline);
            } catch (Exception ex) {
                LOG.error("Can't read pipeline.", ex);
            }
        }
    }

    /**
     *
     * @return A map of all pipelines.
     */
    public Map<String, Pipeline> getPipelines() {
        return Collections.unmodifiableMap(pipelines);
    }

    /**
     * Create and return an empty pipeline.
     *
     * @return
     * @throws IOException
     */
    public synchronized Pipeline createPipeline()
            throws IOException, BaseException {
        // Create uniq identifier.
        String name;
        String iriAsStr;
        do {
            name = "created-" + (new Date()).getTime();
            iriAsStr = configuration.getDomainName()
                    + "/resources/pipelines/" + name;
        } while (pipelines.containsKey(iriAsStr));
        // Create a pipeline.
        final File file = new File(configuration.getPipelinesDirectory(),
                name + ".jsonld");
        final Pipeline pipeline = new Pipeline(file);
        // Load pipeline from an empty definition.
        final Collection<Statement> pipelineRdf = Pipeline.createEmpty(iriAsStr);
        pipeline.setInfo(PipelineLoader.getInfo(pipelineRdf));
        PipelineLoader.updadeReference(pipeline, pipeline.getInfo());
        // Save pipeline to a file.
        RdfUtils.write(file, RDFFormat.JSONLD, pipelineRdf);
        //
        pipelines.put(iriAsStr, pipeline);
        return pipeline;
    }

    /**
     * Update the content of the pipeline. Does not perform any other actions.
     *
     * @param pipeline
     * @param pipelineRdf
     * @throws IOException
     * @throws com.linkedpipes.etl.storage.rdf.RdfUtils.RdfException
     */
    public void updatePipeline(Pipeline pipeline,
            Collection<Statement> pipelineRdf) throws IOException,
            RdfUtils.RdfException, PojoLoader.CantLoadException {
        // Update the reference.
        pipeline.setInfo(PipelineLoader.getInfo(pipelineRdf));
        PipelineLoader.updadeReference(pipeline, pipeline.getInfo());
        // Update the file.
        final RDFFormat format = RdfUtils.getFormat(pipeline.getFile());
        write(pipeline.getFile(), format, pipelineRdf);

        // TODO Update references !!

    }

    /**
     * Delete given pipeline.
     *
     * @param pipeline
     */
    public void deletePipeline(Pipeline pipeline) {
        // TODO Add tomb-stone
        pipeline.getFile().delete();
        pipelines.remove(pipeline.getIri());
    }

    /**
     * Load a pipeline from a given file.
     *
     * @param file
     * @return
     * @throws IOException
     * @throws PojoLoader.CantLoadException
     * @throws com.linkedpipes.etl.storage.rdf.RdfUtils.RdfException
     */
    private static Pipeline loadPipeline(File file)
            throws IOException, PojoLoader.CantLoadException,
            RdfUtils.RdfException {
        final Pipeline pipeline = new Pipeline(file);
        pipeline.setInfo(PipelineLoader.getInfo(RdfUtils.read(file)));
        PipelineLoader.updadeReference(pipeline, pipeline.getInfo());
        return pipeline;
    }

}
