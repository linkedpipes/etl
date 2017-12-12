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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Test that configuration description describe all entities in
 * given configuration class.
 *
 * TODO Check property alternatives IRIs?
 */
public class TestConfigurationDescription {

    public static final String GRAPH = null;

    private List<ConfigurationDescription> descriptions =
            new LinkedList<>();

    public void test(Class<?> configurationClass) throws Exception {
        try {
            loadDescriptions();
        } catch (Exception ex) {
            throw new InvalidDescription(
                    "Can't load configuration description.", ex);
        }
        validateClass(configurationClass);
        validateDescriptorsReference();
    }

    private void loadDescriptions() throws IOException, RdfException,
            InvalidDescription {
        Rdf4jSource source = new Rdf4jSource();
        source.loadFile(getDescriptorFile());
        for (String resource : getDescriptionResources(source)) {
            ConfigurationDescription instance =
                    new ConfigurationDescription(resource);
            RdfToPojoLoader.load(source, resource, null, instance);
            instance.validate();
            descriptions.add(instance);
        }
    }

    private File getDescriptorFile() {
        return TestUtils.fileFromResource("LP-ETL/template/config-desc.ttl");
    }

    private RDFFormat getFormat(File file) {
        return Rio.getParserFormatForFileName(
                file.getName()).orElseThrow(() -> {
            return new RuntimeException("Invalid file: " + file.getName());
        });
    }

    private List<String> getDescriptionResources(RdfSource source)
            throws RdfException {
        return source.getByType(GRAPH, LP_OBJECTS.DESCRIPTION);
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

    private void validateDescriptorsReference() throws IOException,
            InvalidDescription {
        final String reference = readDescriptorReference();
        for (ConfigurationDescription description : descriptions) {
            if (description.getIri().equals(reference)) {
                return;
            }
        }
        throw new InvalidDescription(
                "Missing referenced descriptor (from definition): {}",
                reference);
    }

    private String readDescriptorReference()
            throws InvalidDescription, IOException {
        final Model definition = readDefinition();
        final Resource component = getComponentIri(definition);
        return getReferenceForComponent(definition, component);
    }

    private Model readDefinition() throws IOException {
        final File definitionFile = getDefinitionFile();
        final RDFFormat format = getFormat(definitionFile);
        try (InputStream stream = new FileInputStream(definitionFile)) {
            return Rio.parse(stream, "http://localhost/default", format);
        }
    }

    private File getDefinitionFile() {
        return TestUtils.fileFromResource("LP-ETL/template/definition.jsonld");
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
