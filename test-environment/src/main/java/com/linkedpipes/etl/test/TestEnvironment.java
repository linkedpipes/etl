package com.linkedpipes.etl.test;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.service.WorkingDirectory;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.lang.reflect.Field;

public class TestEnvironment implements AutoCloseable {

    private final SequentialExecution component;

    private final Repository dataRepository;

    private final File componentWorkingDirectory;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    protected TestEnvironment(SequentialExecution component,
            File componentWorkingDirectory) {
        this.component = component;
        this.dataRepository = new SailRepository(new MemoryStore());
        this.dataRepository.initialize();
        this.componentWorkingDirectory = componentWorkingDirectory;
    }

    public void execute() throws Exception {
        bindExtensions();
        component.execute();
    }

    @Override
    public void close() throws Exception {
        if (dataRepository != null && dataRepository.isInitialized()) {
            dataRepository.shutDown();
        }
    }

    public TestFilesDataUnit bindSystemDataUnit(
            String binding, File directory) {
        final TestFilesDataUnit dataUnit = new TestFilesDataUnit(directory);
        bindDataUnit(binding, dataUnit);
        return dataUnit;
    }

    public TestSingleGraphDataUnit bindSingleGraphDataUnit(String binding) {
        final String iri = getIriForBinding(binding);
        final TestSingleGraphDataUnit dataUnit = new TestSingleGraphDataUnit(
                valueFactory.createIRI(iri), dataRepository);
        bindDataUnit(binding, dataUnit);
        return dataUnit;
    }

    public TestGraphListDataUnit bindGraphListDataUnit(String binding) {
        final String iri = getIriForBinding(binding);
        final TestGraphListDataUnit dataUnit = new TestGraphListDataUnit(
                iri, dataRepository);
        bindDataUnit(binding, dataUnit);
        return dataUnit;
    }

    private String getIriForBinding(String binding) {
        if (binding.startsWith("http://")) {
            return binding;
        } else {
            return "http://localhost/test/" + binding;
        }
    }

    private void bindExtensions() throws IllegalArgumentException,
            IllegalAccessException {
        for (Field field : component.getClass().getFields()) {
            if (field.getAnnotation(Component.Inject.class) != null) {
                bindExtension(field);
            }
        }
    }

    private void bindExtension(Field field) throws IllegalAccessException {
        if (field.getType() == ProgressReport.class) {
            field.set(component, new MockedProgressReport());
        } else if (field.getType() == ExceptionFactory.class) {
            field.set(component, new MockedExceptionFactory());
        } else if (field.getType() == WorkingDirectory.class) {
            field.set(component, new WorkingDirectory(
                    componentWorkingDirectory.toURI()));
        } else {
            throw new RuntimeException("Can't initialize extension!");
        }
    }

    /**
     * Set given data unit to the {@link #component}.
     *
     * @param binding
     * @param dataUnit
     */
    private void bindDataUnit(String binding, Object dataUnit) {
        final Field field = getDataUnitField(binding);
        if (field == null) {
            throw new RuntimeException("Invalid binding name:" + binding);
        }
        try {
            field.set(component, dataUnit);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException("Can't bind data unit, "
                    + "check for type and access modifier.", ex);
        }
    }

    private Field getDataUnitField(String iri) {
        for (Field field : component.getClass().getFields()) {
            for (Component.InputPort annotation : field.getAnnotationsByType(
                    Component.InputPort.class)) {
                if (annotation.iri().equals(iri)) {
                    return field;
                }
            }
            for (Component.OutputPort annotation : field.getAnnotationsByType(
                    Component.OutputPort.class)) {
                if (annotation.iri().equals(iri)) {
                    return field;
                }
            }
        }
        return null;
    }

    public static final TestEnvironment create(SequentialExecution component,
            File workingDirectory) {
        return new TestEnvironment(component, workingDirectory);
    }

}
