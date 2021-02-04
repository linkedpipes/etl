package com.linkedpipes.plugin.transformer.rdftojsontemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.Output;
import cz.skodape.hdt.core.PropertySource;
import cz.skodape.hdt.core.SelectorContext;
import cz.skodape.hdt.core.Transform;
import cz.skodape.hdt.json.jackson.JacksonSourceAdapter;
import cz.skodape.hdt.json.java.JsonOutput;
import cz.skodape.hdt.json.java.JsonOutputAdapter;
import cz.skodape.hdt.model.SourceConfiguration;
import cz.skodape.hdt.model.TransformationFile;
import cz.skodape.hdt.model.TransformationFileAdapter;
import cz.skodape.hdt.rdf.rdf4j.Rdf4jChunkedSourceConfiguration;
import cz.skodape.hdt.rdf.rdf4j.Rdf4jMemorySourceConfiguration;
import cz.skodape.hdt.rdf.rdf4j.Rdf4jSourceAdapter;
import cz.skodape.hdt.selector.filter.FilterSelectorAdapter;
import cz.skodape.hdt.selector.identity.IdentitySelectorAdapter;
import cz.skodape.hdt.selector.once.OnceSelectorAdapter;
import cz.skodape.hdt.selector.path.PathSelectorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RdfToJsonTemplate implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(RdfToJsonTemplate.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public RdfToJsonTemplateConfiguration configuration;

    @Override
    public void execute() throws LpException {
        TransformationFile definition = parseDefinition();
        for (FilesDataUnit.Entry entry : inputFiles) {
            SelectorContext context = createContext(definition, entry.toFile());
            File outputFile = outputFiles.createFile(
                    entry.getFileName() + ".json");
            try (FileOutputStream stream = new FileOutputStream(outputFile)) {
                PrintWriter writer = new PrintWriter(
                        stream, true, StandardCharsets.UTF_8);
                Output output = new JsonOutput(writer, true);
                createTransform(definition, context, output).apply();
            } catch (IOException | OperationFailed ex) {
                throw new LpException("Can't transform file.", ex);
            }
            LOG.info("Transforming file: {}", entry.getFileName());
        }
    }

    protected TransformationFile parseDefinition() throws LpException {
        TransformationFileAdapter adapter = createAdapter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(configuration.getMapping());
            return adapter.readJson(root);
        } catch (IOException exception) {
            throw new LpException("Can't read mapping definition.", exception);
        }
    }

    protected TransformationFileAdapter createAdapter() {
        TransformationFileAdapter adapter = new TransformationFileAdapter();
        //
        adapter.addAdapter(new FilterSelectorAdapter());
        adapter.addAdapter(new IdentitySelectorAdapter());
        adapter.addAdapter(new PathSelectorAdapter());
        adapter.addAdapter(new OnceSelectorAdapter());
        //
        adapter.addAdapter(new JsonOutputAdapter());
        adapter.addAdapter(new JacksonSourceAdapter());
        //
        adapter.addAdapter(new Rdf4jSourceAdapter());
        //
        return adapter;
    }

    protected SelectorContext createContext(
            TransformationFile definition, File inputFile) throws LpException {
        Map<String, PropertySource> sources = new HashMap<>();
        for (var entry : definition.sources.entrySet()) {
            SourceConfiguration sourceConfiguration =
                    updateSources(entry.getValue(), inputFile);
            sources.put(entry.getKey(), sourceConfiguration.createSource());
        }
        return new SelectorContext(
                sources, sources.get(definition.propertySource));
    }

    protected SourceConfiguration updateSources(
            SourceConfiguration sourceConfiguration, File inputFile)
            throws LpException {
        if (sourceConfiguration instanceof Rdf4jChunkedSourceConfiguration) {
            Rdf4jChunkedSourceConfiguration config =
                    (Rdf4jChunkedSourceConfiguration) sourceConfiguration;
            config.file = inputFile;
            return config;
        } else if (
                sourceConfiguration instanceof Rdf4jMemorySourceConfiguration) {
            Rdf4jMemorySourceConfiguration config =
                    (Rdf4jMemorySourceConfiguration) sourceConfiguration;
            config.file = inputFile;
            return config;
        } else {
            throw new LpException("Unsupported source configuration.");
        }
    }

    protected Transform createTransform(
            TransformationFile definition,
            SelectorContext context,
            Output output
    ) {
        ConfigurableErrorHandler handler = new ConfigurableErrorHandler();
        handler.setIgnoreMultiplePrimitives(
                configuration.isIgnoreMultiplePrimitives());
        return new Transform(
                definition, context, output, handler);
    }

}
