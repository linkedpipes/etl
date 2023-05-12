package com.linkedpipes.etl.storage.pipeline.repository.file;

import com.linkedpipes.etl.library.pipeline.PipelineLoader;
import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import com.linkedpipes.etl.library.pipeline.adapter.PipelineToRdf;
import com.linkedpipes.etl.library.pipeline.adapter.RdfToRawPipeline;
import com.linkedpipes.etl.library.pipeline.migration.PipelineMigrationFailed;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.ResourceToString;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.PipelineRepository;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Store each pipeline in a single file.
 */
public class FilePipelineRepository implements PipelineRepository {

    private static final Logger LOG =
            LoggerFactory.getLogger(FilePipelineRepository.class);

    protected final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd");

    private final File directory;

    private final TemplateToPlugin templateToPlugin;

    private final AtomicInteger counter = new AtomicInteger();

    private Map<Resource, File> pipelineFiles = Collections.emptyMap();

    public FilePipelineRepository(
            File directory, TemplateToPlugin templateToPlugin) {
        this.directory = directory;
        this.templateToPlugin = templateToPlugin;
    }

    @Override
    public List<StorageException> initializeAndMigrate() {
        directory.mkdirs();
        return reload();
    }

    @Override
    public List<StorageException> reload() {
        return loadAndMigrate();
    }

    private List<StorageException> loadAndMigrate() {
        LOG.debug("Loading repository ... ");
        Map<Resource, File> nextPipelineFiles = new HashMap<>();
        List<StorageException> result = new ArrayList<>();
        List<File> files = listPipelineFiles();
        int migratedCounter = 0;
        for (File file : files) {
            RawPipeline rawPipeline;
            try {
                rawPipeline = loadRawPipeline(file);
            } catch (StorageException ex) {
                result.add(new StorageException(
                        "Can't load from '{}'.", file, ex));
                continue;
            }
            if (Pipeline.VERSION == rawPipeline.version) {
                nextPipelineFiles.put(rawPipeline.resource, file);
                continue;
            }
            PipelineLoader loader;
            try {
                loader = createLoader();
            } catch (StorageException ex) {
                result.add(new StorageException(
                        "Can't prepare loader for '{}'.",
                        rawPipeline.resource, ex));
                continue;
            }
            Pipeline migrated;
            try {
                migrated = loader.loadPipeline(rawPipeline);
            } catch (PipelineMigrationFailed ex) {
                result.add(new StorageException(
                        "Can't migrate pipeline '{}' from '{}'.",
                        rawPipeline.resource, file, ex));
                continue;
            }
            nextPipelineFiles.put(rawPipeline.resource, file);
            try {
                handleMigrated(migrated, file);
            } catch (StorageException ex) {
                result.add(ex);
            }
            ++migratedCounter;
        }
        pipelineFiles = nextPipelineFiles;
        LOG.debug("Loading repository ... done " +
                        "(files: {}, loaded: {}, migrated: {}, failed: {})",
                files.size(),
                pipelineFiles.size(),
                migratedCounter,
                result.size());
        return result;
    }

    protected List<File> listPipelineFiles() {
        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(files)
                .filter(File::isFile)
                .filter(file -> !isBackupFile(file))
                .filter(file -> !isSwapFile(file))
                .toList();
    }

    protected boolean isBackupFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".backup");
    }

    protected boolean isSwapFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".swp");
    }

    protected RawPipeline loadRawPipeline(File file) throws StorageException {
        Statements statements = Statements.arrayList();
        try {
            statements.file().addAllIfExists(file);
        } catch (IOException ex) {
            throw new StorageException("Can't read file.", ex);
        }
        List<RawPipeline> candidates =
                RdfToRawPipeline.asRawPipelines(statements.selector());
        if (candidates.size() != 1) {
            throw new StorageException(
                    "Invalid number of pipelines '{}', expected one.",
                    candidates.size());
        }
        return candidates.get(0);
    }

    protected PipelineLoader createLoader() throws StorageException {
        return new PipelineLoader(templateToPlugin.getTemplateToPluginMap());
    }

    private void handleMigrated(Pipeline pipeline, File file)
            throws StorageException {
        // Create a backup.
        File backUpFile = createBackupFile(file);
        if (!backUpFile.exists()) {
            try {
                Files.copy(file.toPath(), backUpFile.toPath());
            } catch (IOException ex) {
                throw new StorageException("Can't create pipeline backup.", ex);
            }
        }
        writePipelineToFile(file, pipeline);
    }

    protected File createBackupFile(File file) {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        fileName += "_" + dateFormat.format(new Date());
        fileName += ".trig.backup";
        return new File(file.getParent(), fileName);
    }

    protected void writePipelineToFile(File file, Pipeline pipeline)
            throws StorageException {
        Statements statements  = PipelineToRdf.asRdf(pipeline);
        try {
            statements.file().atomicWriteToFile(file, RDFFormat.TRIG);
        } catch (IOException | RuntimeException ex) {
            throw new StorageException(
                    "Can't write pipeline '{}' to file '{}'.",
                    pipeline.resource(), file, ex);
        }
    }

    @Override
    public Set<Resource> listPipelines() {
        return pipelineFiles.keySet();
    }

    @Override
    public Pipeline loadPipeline(Resource resource)
            throws StorageException {
        File file = pipelineFiles.get(resource);
        if (file == null) {
            return null;
        }
        RawPipeline rawPipeline = loadRawPipeline(file);
        return rawPipeline.toPipeline();
    }

    @Override
    public void storePipeline(Pipeline pipeline)
            throws StorageException {
        File file = pipelineFiles.computeIfAbsent(
                pipeline.resource(), resource -> createNewFile(pipeline));
        writePipelineToFile(file, pipeline);
    }

    protected File createNewFile(Pipeline pipeline) {
        // Otherwise, just encode the whole value.
        String fileName = ResourceToString.asBase64Full(pipeline.resource());
        return new File(directory, fileName + ".trig");
    }

    @Override
    public void deletePipeline(Resource resource)
            throws StorageException {
        File file = pipelineFiles.get(resource);
        if (file == null) {
            pipelineFiles.remove(resource);
            return;
        }
        if (!file.delete()) {
            throw new StorageException(
                    "Can't delete pipeline file '{}'.", file);
        }
        pipelineFiles.remove(resource);
    }

    @Override
    public Resource reserveResource(ResourceFactory factory, String baseUrl) {
        String time = String.valueOf(new Date().getTime());
        String index = String.format("%1$4s", counter.incrementAndGet())
                .replace(" ", "0");
        String suffix = time + "-" + index;
        return factory.apply(baseUrl, suffix);
    }

}
