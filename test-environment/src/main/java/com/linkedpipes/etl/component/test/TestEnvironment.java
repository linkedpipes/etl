package com.linkedpipes.etl.component.test;

import com.linkedpipes.etl.dataunit.sesame.GraphListDataUnitImpl;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.RdfDataUnitConfiguration;
import com.linkedpipes.etl.dataunit.sesame.SingleGraphDataUnitImpl;
import com.linkedpipes.etl.dataunit.system.FilesDataUnitConfiguration;
import com.linkedpipes.etl.dataunit.system.FilesDataUnitImpl;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.Component.InputPort;
import com.linkedpipes.etl.component.api.Component.OutputPort;
import com.linkedpipes.etl.component.api.service.AfterExecution;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import java.io.File;
import java.lang.reflect.Field;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.WorkingDirectory;

/**
 *
 * @author Petr Škoda
 */
public class TestEnvironment implements AutoCloseable {

    private static final String IRI_PREFIX = "http://localhost/test/";

    private final Component.Sequential component;

    private final Repository sesameRepository;

    private Integer suffixCounter = 0;

    private final MockedAfterExecution afterExecution
            = new MockedAfterExecution();

    private final File workingDirectory;

    protected TestEnvironment(Component.Sequential dpu, File workingDirectory) {
        this.component = dpu;
        this.sesameRepository = new SailRepository(new MemoryStore());
        this.sesameRepository.initialize();
        this.workingDirectory = workingDirectory;
    }

    /**
     * Execute the DPU with given configuration.
     *
     * @throws java.lang.Exception
     */
    public void execute() throws Exception {
        bindExtensions();
        try {
            component.execute();
        } finally {
            // Execute after execution.
            afterExecution.execute();
        }
    }

    @Override
    public void close() throws Exception {
        if (sesameRepository != null && sesameRepository.isInitialized()) {
            sesameRepository.shutDown();
        }
    }

    /**
     * Create and return system file data unit.
     *
     * @param binding
     * @param workingDirectory
     * @return
     */
    public WritableFilesDataUnit bindSystemDataUnit(String binding,
            File workingDirectory) {
        final IRI dataUnitIri = getUri("dataUnit");
        final FilesDataUnitConfiguration configuration
                = new FilesDataUnitConfiguration(
                        dataUnitIri.stringValue(),
                        binding,
                        workingDirectory.toURI().toString());
        final WritableFilesDataUnit dataUnit
                = new FilesDataUnitImpl(configuration);
        bindDataUnit(binding, dataUnit);
        return dataUnit;
    }

    /**
     * Create and return Sesame single graph RDF data unit.
     *
     * @param binding
     * @return
     */
    public WritableSingleGraphDataUnit bindSingleGraphDataUnit(String binding) {
        final IRI dataUnitIri = getUri("dataUnit");
        final RdfDataUnitConfiguration configuration = new RdfDataUnitConfiguration(
                dataUnitIri.stringValue(), binding);
        final WritableSingleGraphDataUnit dataUnit = new SingleGraphDataUnitImpl(
                dataUnitIri, sesameRepository, configuration);
        bindDataUnit(binding, dataUnit);
        return dataUnit;
    }

    /**
     * Create and return Sesame multiple graph RDF data unit.
     *
     * @param binding
     * @return
     */
    public WritableGraphListDataUnit bindGraphListDataUnit(String binding) {
        final IRI dataUnitIri = getUri("dataUnit");
        final RdfDataUnitConfiguration configuration
                = new RdfDataUnitConfiguration(
                        dataUnitIri.stringValue(), binding);
        final WritableGraphListDataUnit dataUnit = new GraphListDataUnitImpl(
                dataUnitIri, sesameRepository, configuration);
        bindDataUnit(binding, dataUnit);
        return dataUnit;
    }

    ;

    /**
     * Bind extensions to current {@link #component}.
     */
    private void bindExtensions() throws IllegalArgumentException,
            IllegalAccessException {
        for (Field field : component.getClass().getFields()) {
            if (field.getAnnotation(Component.Inject.class) != null) {
                // Based on type set extension.
                if (field.getType() == ProgressReport.class) {
                    field.set(component, new MockedProgressReport());
                } else if (field.getType() == AfterExecution.class) {
                    field.set(component, afterExecution);
                } else if (field.getType() == ExceptionFactory.class) {
                    field.set(component, new MockedExceptionFactory());
                } else if (field.getType() == WorkingDirectory.class) {
                    field.set(component, new WorkingDirectory(
                            workingDirectory.getPath()));
                } else {
                    throw new RuntimeException("Can't initialize extension!");
                }
            }
        }
    }

    /**
     *
     * @param type
     * @return Newly generated unique URI that contains given type.
     */
    private IRI getUri(String type) {
        suffixCounter++;
        return SimpleValueFactory.getInstance().createIRI(
                IRI_PREFIX + type + "/" + suffixCounter);
    }

    /**
     *
     *
     * @param name
     * @return Field for data unit with given name, or null.
     */
    private Field getDataUnitField(String name) {
        for (Field field : component.getClass().getFields()) {
            for (InputPort annotation : field.getAnnotationsByType(
                    Component.InputPort.class)) {
                if (annotation.id().equals(name)) {
                    return field;
                }
            }
            for (OutputPort annotation : field.getAnnotationsByType(
                    Component.OutputPort.class)) {
                if (annotation.id().equals(name)) {
                    return field;
                }
            }
        }
        return null;
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
                    + "check for type and acess modifier.", ex);
        }
    }

    /**
     * Create test environment.
     *
     * @param dpu
     * @param workingDirectory
     * @return
     */
    public static final TestEnvironment create(Component.Sequential dpu,
            File workingDirectory) {
        return new TestEnvironment(dpu, workingDirectory);
    }

}
