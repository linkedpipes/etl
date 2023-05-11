package com.linkedpipes.etl.test.suite;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.test.TestUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Test that configuration description describes all entities in
 * given configuration class.
 */
public class TestConfigurationDescription {

    private final List<ConfigurationDescription> descriptions =
            new LinkedList<>();

    public void test(Class<?> configurationClass) throws Exception {
        // For backwards compatibility, where there was only one directory.
        test(configurationClass, "template");
    }

    public void test(Class<?> configurationClass, String directory)
            throws Exception {
        try {
            loadDescriptions(directory);
        } catch (Exception ex) {
            throw new InvalidDescription(
                    "Can't load configuration description.", ex);
        }
        validateClass(configurationClass);
        validateDescriptorsReference(directory);
    }

    private void loadDescriptions(String directory) throws IOException, RdfException,
            InvalidDescription {
        Rdf4jSource source = loadDescriptorFile(directory);
        for (String resource : getDescriptionResources(source)) {
            ConfigurationDescription instance =
                    new ConfigurationDescription(resource);
            RdfToPojoLoader.load(source, resource, instance);
            instance.validate();
            descriptions.add(instance);
        }
    }

    private Rdf4jSource loadDescriptorFile(String directory) throws IOException {
        for (String name : getDescriptorFilesName(directory)) {
            File file;
            try {
                file = TestUtils.fileFromResource(name);
            } catch (RuntimeException ex) {
                // This can fail when resource is missing.
                continue;
            }
            Rdf4jSource result = new Rdf4jSource();
            result.loadFile(file);
            return result;
        }
        throw new FileNotFoundException("Missing descriptor file.");
    }

    private List<String> getDescriptorFilesName(String directory) {
        return Arrays.asList(
                "LP-ETL/" + directory + "/config-desc.ttl",
                "LP-ETL/" + directory + "/configuration-description.ttl");
    }

    private RDFFormat getFormat(File file) {
        return Rio.getParserFormatForFileName(file.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Invalid file: " + file.getName()));
    }

    private List<String> getDescriptionResources(RdfSource source)
            throws RdfException {
        return source.getByType(LP_OBJECTS.DESCRIPTION);
    }

    private void validateClass(Class<?> objectClass) throws InvalidDescription {
        final String type = getType(objectClass);
        final ConfigurationDescription description = getDescriptor(type);
        validateProperties(objectClass, description);
    }

    private String getType(Class<?> objectClass) throws InvalidDescription {
        final RdfToPojo.Type type =
                objectClass.getAnnotation(RdfToPojo.Type.class);
        if (type == null) {
            throw new InvalidDescription(
                    "Missing RdfToPojo.Type annotation: {}",
                    objectClass.getName());
        }
        return type.iri();
    }

    private ConfigurationDescription getDescriptor(String type)
            throws InvalidDescription {
        for (ConfigurationDescription description : descriptions) {
            if (description.getReferencedType().equals(type)) {
                return description;
            }
        }
        throw new InvalidDescription("Missing description for: " + type);
    }

    private void validateProperties(Class<?> objectClass,
            ConfigurationDescription description)
            throws InvalidDescription {
        for (Map.Entry<String, Class> entry :
                getAnnotatedFields(objectClass).entrySet()) {
            final ConfigurationDescription.Member member =
                    description.getMember(entry.getKey());
            if (member.isComplex()) {
                validateClass(entry.getValue());
            }
        }
    }

    private Map<String, Class> getAnnotatedFields(Class<?> objectClass) {
        final Map<String, Class> result = new HashMap<>();
        for (Field field : objectClass.getDeclaredFields()) {
            final RdfToPojo.Property property =
                    field.getAnnotation(RdfToPojo.Property.class);
            if (property == null) {
                break;
            }
            result.put(property.iri(), getFieldType(field));
        }
        return result;
    }

    private Class getFieldType(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            return getCollectionType(field.getGenericType());
        } else {
            return field.getType();
        }
    }

    private static Class<?> getCollectionType(Type type) {
        final ParameterizedType parameterizedType =
                (ParameterizedType) type;
        final Type[] params = parameterizedType.getActualTypeArguments();
        return (Class<?>) params[0];
    }

    private void validateDescriptorsReference(String directory)
            throws IOException,
            InvalidDescription {
        final String reference = readDescriptorReference(directory);
        for (ConfigurationDescription description : descriptions) {
            if (description.getIri().equals(reference)) {
                return;
            }
        }
        throw new InvalidDescription(
                "Missing referenced descriptor (from definition): {}",
                reference);
    }

    private String readDescriptorReference(String directory)
            throws InvalidDescription, IOException {
        final Model definition = readDefinition(directory);
        final Resource component = getComponentIri(definition);
        return getReferenceForComponent(definition, component);
    }

    private Model readDefinition(String directory) throws IOException {
        final File definitionFile = getDefinitionFile(directory);
        final RDFFormat format = getFormat(definitionFile);
        try (InputStream stream = new FileInputStream(definitionFile)) {
            return Rio.parse(stream, "http://localhost/default", format);
        }
    }

    private File getDefinitionFile(String directory) {
        return TestUtils.fileFromResource(
                "LP-ETL/" + directory + "/definition.jsonld");
    }

    private Resource getComponentIri(Model definition)
            throws InvalidDescription {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final Model component = definition.filter(null, RDF.TYPE,
                valueFactory.createIRI(LP_PIPELINE.JAS_TEMPLATE));
        if (component.size() != 1) {
            throw new InvalidDescription(
                    "Invalid count of component descriptions: {}",
                    component.size());
        }
        return component.subjects().iterator().next();
    }

    private String getReferenceForComponent(Model definition,
            Resource component) throws InvalidDescription {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final Model references = definition.filter(component,
                valueFactory.createIRI(
                        LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION),
                null);
        if (references.size() != 1) {
            throw new InvalidDescription(
                    "Invalid description references from component: {}",
                    references.size());
        }
        return references.objects().iterator().next().stringValue();
    }

}
