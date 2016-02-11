package com.linkedpipes.commons.code.configuration.boundary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public abstract class AbstractConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractConfiguration.class);

    @FunctionalInterface
    protected static interface Validator<T> {

        void validate(T value) throws Exception;

    }

    private final Properties properties = new Properties();

    @PostConstruct
    public void init() {
        final String propertiesFile = System.getProperty("configFileLocation");
        if (propertiesFile == null) {
            LOG.error("Missing property '-configFileLocation' with path to configuration file.");
            throw new RuntimeException("Missing configuration file.");
        }
        LOG.info("Reading configuration file: {}", propertiesFile);
        // Read properties.
        try (InputStream stream = new FileInputStream(new File(propertiesFile))) {
            properties.load(stream);
        } catch (IOException ex) {
            throw new RuntimeException("Can't load configuration file.", ex);
        }
        // Load properties.
        loadProperties();
    }

    abstract protected void loadProperties();

    protected Validator<String> validateDirectory() {
        return (value) -> {
            (new File(value)).mkdirs();
        };
    }

    protected Validator<String> validateUri() {
        return (value) -> {
            new URI(value);
        };
    }

    protected String getProperty(String name) {
        final String value;
        try {
            value = properties.getProperty(name);
        } catch (RuntimeException ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw ex;
        }
        if (value == null) {
            LOG.error("Missing configuration property: '{}'", name);
            throw new RuntimeException("Missing configuration property!");
        } else {
            return value;
        }
    }

    protected String getProperty(String name, Validator<String> validator) {
        final String value = getProperty(name);
        try {
            validator.validate(value);
        } catch (Exception ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
        return value;
    }

    protected Integer getPropertyInteger(String name) {
        final String value = getProperty(name);
        try {
            final Integer valueAsInteger = Integer.parseInt(value);
            return valueAsInteger;
        } catch (Exception ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
    }

    protected Integer getPropertyInteger(String name, Validator<Integer> validator) {
        final String value = getProperty(name);
        try {
            final Integer valueAsInteger = Integer.parseInt(value);
            validator.validate(valueAsInteger);
            return valueAsInteger;
        } catch (Exception ex) {
            LOG.error("Invalid configuration property: '{}'", name);
            throw new RuntimeException(ex);
        }
    }

}
