package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.pipeline.transformation.TransformationFacade;
import com.linkedpipes.etl.storage.pipeline.transformation.TransformationFailed;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Load pipeline from a file.
 * If the pipeline version is not actual create backup of the pipeline
 * file and migrate the current pipeline.
 */
class PipelineLoader {

    private final SimpleDateFormat dateFormat
            = new SimpleDateFormat("yyyy-MM-dd");

    private File file;

    private PipelineInfo info;

    private Collection<Statement> pipelineRdf;

    private final TransformationFacade transformation;

    public PipelineLoader(TransformationFacade transformationFacade) {
        this.transformation = transformationFacade;
    }

    public Pipeline load(File file) throws OperationFailed {
        this.file = file;
        //
        loadFromFile();
        if (info.getVersion() != Pipeline.VERSION_NUMBER) {
            migratePipeline();
        }
        return new Pipeline(file, info);
    }

    private void loadFromFile() throws OperationFailed {
        try {
            pipelineRdf = RdfUtils.read(file);
            loadPipelineInfo();
        } catch (PojoLoader.CantLoadException | RdfUtils.RdfException ex) {
            throw new OperationFailed("Can't load pipeline: {}", file, ex);
        }
    }

    private void loadPipelineInfo() throws PojoLoader.CantLoadException {
        info = new PipelineInfo();
        PojoLoader.loadOfType(pipelineRdf, Pipeline.TYPE, info);
    }

    private void migratePipeline() throws OperationFailed {
        backupPipeline();
        try {
            pipelineRdf = transformation.migrateThrowOnWarning(pipelineRdf);
            loadPipelineInfo();
            updatePipelineFile();
        } catch (TransformationFailed | PojoLoader.CantLoadException ex) {
            throw new OperationFailed(
                    "Can't update pipeline: {}", file, ex);
        }
    }

    private void backupPipeline() throws OperationFailed {
        File backupFile = getBackupFile();
        try {
            RdfUtils.write(backupFile, RDFFormat.TRIG, pipelineRdf);
        } catch (RdfUtils.RdfException ex) {
            throw new OperationFailed(
                    "Can't pipeline backup: {}", backupFile, ex);
        }
    }

    private File getBackupFile() {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        fileName += "_" + dateFormat.format(new Date());
        fileName += ".trig.backup";
        return new File(file.getParent(), fileName);
    }

    private void updatePipelineFile() throws OperationFailed {
        try {
            RdfUtils.write(file, RDFFormat.TRIG, pipelineRdf);
        } catch (RdfUtils.RdfException ex) {
            throw new OperationFailed(
                    "Can't save updated pipeline to file: {}", file, ex);
        }
    }

    public Collection<Statement> getPipelineRdf() {
        return pipelineRdf;
    }

}
