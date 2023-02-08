package com.linkedpipes.etl.executor.monitor.cli;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.cli.adapter.EnvironmentToConfiguration;
import com.linkedpipes.etl.executor.monitor.cli.adapter.PropertiesToConfiguration;
import com.linkedpipes.etl.executor.monitor.cli.adapter.RdfToConfiguration;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConfigurationLoader {

    private static final Logger LOG =
            LoggerFactory.getLogger(ConfigurationLoader.class);

    private Configuration configuration;

    public String[] load(String[] args) throws MonitorException {
        createDefault();
        String[] nextArgs = loadFromArgs(args);
        loadFileFromEnvironment();
        loadFromEnvironment();
        return nextArgs;
    }

    private void createDefault() {
        configuration = new Configuration();
    }

    private String[] loadFromArgs(String[] args) throws MonitorException {
        List<String> result = new ArrayList<>(Arrays.asList(args));
        for (int index = 0; index < result.size(); ++index) {
            String item = result.get(index);
            if (item.startsWith("--configuration-file")) {
                String[] tokens = item.split("=", 2);
                if (tokens.length < 2) {
                    throw new MonitorException("Invalid argument: {}", item);
                }
                loadFromFile(new File(tokens[1]));
                result.remove(index);
                return result.toArray(new String[0]);
            }
            if (!item.startsWith("-")) {
                // Read only till the command.
                break;
            }
        }
        return result.toArray(new String[0]);
    }

    private void loadFromFile(File file) throws MonitorException {
        LOG.debug("Reading configuration from: {}", file);
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".properties")) {
            LOG.warn("Properties configuration files are deprecated.");
            configuration = PropertiesToConfiguration
                    .updateConfiguration(configuration, file);
            return;
        }
        Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
        if (format.isPresent()) {
            configuration = RdfToConfiguration
                    .updateConfiguration(configuration, file, format.get());
            return;
        }
        throw new MonitorException("Unknown configuration file type.");
    }

    private void loadFileFromEnvironment() throws MonitorException {
        String file = System.getProperty("configFileLocation");
        if (file == null) {
            return;
        }
        loadFromFile(new File(file));
    }

    private void loadFromEnvironment() throws MonitorException {
        configuration = EnvironmentToConfiguration
                .updateConfiguration(configuration);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
