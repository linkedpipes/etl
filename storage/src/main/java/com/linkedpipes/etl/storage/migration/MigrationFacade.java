package com.linkedpipes.etl.storage.migration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.component.pipeline.Pipeline;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.rdf.StatementsCollection;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Petr Škoda
 */
public final class MigrationFacade {

    private static final DateFormat DATE_FORMAT
            = new SimpleDateFormat("YYYY-MM-dd_HH-mm-ss");

    private static final Logger LOG
            = LoggerFactory.getLogger(MigrationFacade.class);

    public static final IRI COMPONENT;


    public static final IRI HAS_TEMPLATE;

    static {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        COMPONENT = vf.createIRI(
                "http://linkedpipes.com/ontology/Component");
        HAS_TEMPLATE = vf.createIRI(
                "http://linkedpipes.com/ontology/template");
    }

    private MigrationFacade() {

    }

    /**
     * Update files from JSON format to JSON-LD.
     *
     * @param file
     * @return Null if there is problem with migration.
     */
    public static File migrateJsonPipeline(File file) {
        // We know the format is JSON-LD.
        final RDFParser parser = Rio.createParser(RDFFormat.JSONLD);
        final List<Statement> pipeline = new LinkedList<>();
        try (InputStream stream = new FileInputStream(file)) {
            parser.setRDFHandler(new StatementCollector(pipeline));
            parser.parse(stream, "http://localhost/base");
        } catch (IOException ex) {
            LOG.error("Can't read JSON (JSON-LD) pipeline.", ex);
            return null;
        }
        // Perform standard update.
        final Collection<Statement> newPipeline;
        try {
            newPipeline = migratePipeline(pipeline);
        } catch (Exception ex) {
            LOG.error("Can't migrate pipeline: {}", file, ex);
            return null;
        }
        // Write to a new file.
        final File newFile = new File(file.getParentFile(),
                file.getName() + "ld");
        try (OutputStream stream = new FileOutputStream(newFile)) {
            Rio.write(newPipeline, stream, RDFFormat.JSONLD);
        } catch (IOException ex) {
            return null;
        }
        // Rename old file.
        file.renameTo(new File(file.getParentFile(),
                file.getName() + "_" + DATE_FORMAT.format(new Date()) +
                        ".backup"));
        return newFile;
    }

    /**
     * Create new pipeline from given pipeline that is of current version
     * of LP.
     *
     * @param pipelineRdf
     * @return
     */
    public static Collection<Statement> migratePipeline(
            Collection<Statement> pipelineRdf) throws BaseException {
        // Extract pipeline object.
        final Resource pipelineResource = RdfUtils.find(pipelineRdf,
                Pipeline.TYPE);
        if (pipelineRdf == null) {
            throw new BaseException("Missing pipeline resource.");
        }
        final StatementsCollection all = new StatementsCollection(pipelineRdf);
        final StatementsCollection configurations = all.filter(
                (s) -> !s.getContext().equals(pipelineResource));
        final RdfObjects pipelineObject = new RdfObjects(all.filter(
                (s) -> s.getContext().equals(pipelineResource)).getStatements());
        final RdfObjects.Entity pipeline
                = pipelineObject.getTypeSingle(Pipeline.TYPE);
        int version;
        try {
            final Value value = pipeline.getProperty(Pipeline.HAS_VERSION);
            version = ((Literal)value).intValue();
        } catch (Exception ex) {
            version = 0;
        }
        LOG.info("Migrating pipeline {} from version {}",
                pipelineResource, version);
        // Perform update.
        switch (version) {
            case 0:
                updateFrom1(pipelineObject, pipeline, configurations);
            case 1: // Current version.
                break;
            default:
                throw new BaseException("Invalid version ({}) for pipeline: {}",
                        version, pipelineResource);
        }
        // Replace information about version.
        pipeline.delete(Pipeline.HAS_VERSION);
        pipeline.add(Pipeline.HAS_VERSION, Pipeline.VERSION_NUMBER);
        // Create output representation.
        final List<Statement> output = new LinkedList<>();
        output.addAll(pipelineObject.asStatements(pipelineResource));
        output.addAll(configurations.getStatements());
        return output;
    }

    /**
     * Perform inplace update from version 0 to version 1. Does not change
     * pipeline version property.
     *
     * @param pipelineObject
     * @param pipeline
     * @param configurations
     */
    private static void updateFrom1(RdfObjects pipelineObject,
            RdfObjects.Entity pipeline,StatementsCollection configurations) {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        for (RdfObjects.Entity entity :
                pipelineObject.getTyped(COMPONENT)) {
            final List<Resource> newTemplates = new LinkedList<>();
            entity.getReferences(HAS_TEMPLATE).forEach((ref) -> {
                // Example of conversion:
                // http://localhost:8080/resources/components/t-tabular
                // http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0
                String template = ref.getResource().stringValue();
                String name  = template.substring(template.lastIndexOf("/") + 1);
                template = "http://etl.linkedpipes.com/resources/components/"
                    + name + "/0.0.0";
                newTemplates.add(vf.createIRI(template));
            });
            entity.deleteReferences(HAS_TEMPLATE);
            newTemplates.forEach((e) -> entity.add(HAS_TEMPLATE, e));
        }
    }

}
