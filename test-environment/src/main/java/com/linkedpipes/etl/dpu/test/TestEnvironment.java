package com.linkedpipes.etl.dpu.test;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.rdf.RdfDataUnitConfiguration;
import com.linkedpipes.etl.dataunit.sesame.rdf.SesameRdfDataUnitFactory;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.system.files.FilesDataUnitConfiguration;
import com.linkedpipes.etl.dataunit.system.files.SystemFilesDataUnitFactory;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit.InputPort;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit.OutputPort;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.AfterExecution;
import com.linkedpipes.etl.dpu.api.extensions.FaultTolerance;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
import java.io.File;
import java.lang.reflect.Field;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class TestEnvironment implements AutoCloseable {

    private static final String IRI_PREFIX = "http://localhost/test/";

    private static final Logger LOG = LoggerFactory.getLogger(TestEnvironment.class);

    private final SequentialExecution dpu;

    private final DataProcessingUnit.Context context;

    private final Repository sesameRepository;

    private Integer suffixCounter = 0;

    private final MockedAfterExecution afterExecution = new MockedAfterExecution();

    protected TestEnvironment(SequentialExecution dpu, File workingDirectory) {
        this.dpu = dpu;
        this.context = new TestContext(IRI_PREFIX + "component", workingDirectory);
        sesameRepository = new SailRepository(new MemoryStore());
        sesameRepository.initialize();
    }

    /**
     * Execute the DPU with given configuration.
     *
     * @throws java.lang.Exception
     */
    public void execute() throws Exception {
        bindExtensions();
        try {
            dpu.execute(context);
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
    public WritableFilesDataUnit bindSystemDataUnit(String binding, File workingDirectory) {
        final IRI dataUnitIri = getUri("dataUnit");
        final FilesDataUnitConfiguration configuration = new FilesDataUnitConfiguration(
                dataUnitIri.stringValue(),
                binding,
                workingDirectory.toURI().toString(),
                null);
        final WritableFilesDataUnit dataUnit = SystemFilesDataUnitFactory.create(configuration);
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
                dataUnitIri.stringValue(), binding, null);
        final WritableSingleGraphDataUnit dataUnit = SesameRdfDataUnitFactory.createSingleGraph(
                dataUnitIri,
                sesameRepository,
                configuration);
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
        final RdfDataUnitConfiguration configuration = new RdfDataUnitConfiguration(
                dataUnitIri.stringValue(), binding, null);
        final WritableGraphListDataUnit dataUnit = SesameRdfDataUnitFactory.createGraphList(
                dataUnitIri,
                sesameRepository,
                configuration);
        bindDataUnit(binding, dataUnit);
        return dataUnit;
    }

    ;

    /**
     * Bind extensions to current {@link #dpu}.
     */
    private void bindExtensions() throws IllegalArgumentException, IllegalAccessException {
        for (Field field : dpu.getClass().getFields()) {
            if (field.getAnnotation(DataProcessingUnit.Extension.class) != null) {
                // Based on type set extension.
                if (field.getType() == FaultTolerance.class) {
                    field.set(dpu, new MockedFaultTolerance());
                } else if (field.getType() == ProgressReport.class) {
                    field.set(dpu, new MockedProgressReport());
                } else if (field.getType() == AfterExecution.class) {
                    field.set(dpu, afterExecution);
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
        return SimpleValueFactory.getInstance().createIRI(IRI_PREFIX + type + "/" + suffixCounter);
    }

    /**
     *
     *
     * @param name
     * @return Field for data unit with given name, or null if no such field can be found.
     */
    private Field getDataUnitField(String name) {
        for (Field field : dpu.getClass().getFields()) {
            for (InputPort annotation : field.getAnnotationsByType(DataProcessingUnit.InputPort.class)) {
                if (annotation.id().equals(name)) {
                    return field;
                }
            }
            for (OutputPort annotation : field.getAnnotationsByType(DataProcessingUnit.OutputPort.class)) {
                if (annotation.id().equals(name)) {
                    return field;
                }
            }
        }
        return null;
    }

    /**
     * Set given data unit to the {@link #dpu}.
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
            field.set(dpu, dataUnit);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException("Can't bind data unit, check for type and acess modifier.", ex);
        }
    }

    /**
     * Create test environment.
     *
     * @param dpu
     * @param workingDirectory
     * @return
     */
    public static final TestEnvironment create(SequentialExecution dpu, File workingDirectory) {
        return new TestEnvironment(dpu, workingDirectory);
    }

}
