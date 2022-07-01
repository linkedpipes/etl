package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.RuntimeConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.service.ServiceFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Implementation for default manageable wrap for sequential component.
 */
class SequentialWrap implements
        ManageableComponent, SequentialExecution, ResumableComponent {

    private final SequentialExecution component;

    private final String componentIri;

    private final RdfSource definition;

    private final ServiceFactory serviceFactory;

    public SequentialWrap(
            SequentialExecution component, String componentIri,
            RdfSource definition, ServiceFactory serviceFactory) {
        this.component = component;
        this.componentIri = componentIri;
        this.definition = definition;
        this.serviceFactory = serviceFactory;
    }

    @Override
    public void resumeExecution(File previousWorkingDirectory)
            throws LpException {
        if (component instanceof ResumableComponent) {
            ((ResumableComponent)component)
                    .resumeExecution(previousWorkingDirectory);
        }
    }

    @Override
    public void execute(Component.Context context) throws LpException {
        try {
            component.execute(context);
        } catch (Throwable t) {
            throw new LpException("Execution failed.", t);
        }
    }

    @Override
    public void initialize(
            Map<String, DataUnit> dataUnits, Component.Context context)
            throws LpException {
        // Bind ports.
        bingPorts(dataUnits);
        // Inject services.
        injectServices(context);
    }

    @Override
    public void loadConfiguration(RdfSource definition)
            throws LpException {
        // Load configuration.
        for (Field field : component.getClass().getFields()) {
            if (field.getAnnotation(Component.Configuration.class) != null) {
                loadConfigurationForField(field, definition);
            }
        }
    }

    @Override
    public RuntimeConfiguration getRuntimeConfiguration() throws LpException {
        final Object fieldValue = getConfigurationObject();
        if (fieldValue == null) {
            return null;
        }
        if (fieldValue instanceof RuntimeConfiguration) {
            return (RuntimeConfiguration) fieldValue;
        }
        throw new LpException("Invalid configuration object type: {}",
                fieldValue.getClass());
    }

    /**
     * Load configuration for given field.
     */
    private void loadConfigurationForField(Field field, RdfSource definition)
            throws LpException {
        final Object instance;
        try {
            instance = field.getType().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new LpException("Can't create instance of {} for {}",
                    field.getType().getSimpleName(), field.getName(), ex);
        }
        RdfToPojoLoader.loadByReflection(definition, instance);
        try {
            field.set(component, instance);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new LpException("Can't set value to {}",
                    field.getName(), ex);
        }
    }

    /**
     * Bind all ports (dataunits) fields.
     */
    private void bingPorts(Map<String, DataUnit> dataUnits) throws LpException {
        for (Field field : component.getClass().getFields()) {
            Component.InputPort input =
                    field.getAnnotation(Component.InputPort.class);
            if (input != null) {
                bindPort(dataUnits, field, input.iri());
            }
            Component.OutputPort output =
                    field.getAnnotation(Component.OutputPort.class);
            if (output != null) {
                bindPort(dataUnits, field, output.iri());
            }
        }
    }

    /**
     * Bind data unit for to given field.
     */
    private void bindPort(
            Map<String, DataUnit> dataUnits, Field field, String id)
            throws LpException {
        DataUnit dataUnit = null;
        for (DataUnit item : dataUnits.values()) {
            if (id.equals(item.getBinding())) {
                dataUnit = item;
                break;
            }
        }
        if (dataUnit == null) {
            throw new LpException("Missing data unit: {}", id);
        }
        if (!field.getType().isAssignableFrom(dataUnit.getClass())) {
            throw new LpException("Dataunit type mismatch ({}): {} -> {}",
                    id, dataUnit.getClass().getSimpleName(),
                    field.getType().getSimpleName());
        }
        try {
            field.set(component, dataUnit);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new LpException("Can't set data unit: {}", id, ex);
        }
    }

    /**
     * Construct and inject services.
     */
    private void injectServices(Component.Context context) throws LpException {
        for (Field field : component.getClass().getFields()) {
            if (field.getAnnotation(Component.Inject.class) == null) {
                continue;
            }
            Object instance;
            try {
                instance = serviceFactory.create(
                        field.getType(), componentIri, definition, context);
            } catch (LpException ex) {
                throw new LpException("Can't instantiate: {} : {}",
                        field.getName(), field.getType().getSimpleName(), ex);
            }
            try {
                field.set(component, instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new LpException("Can't inject object: {} of {}",
                        field.getName(), instance.getClass().getSimpleName(),
                        ex);
            }
        }
    }

    private Object getConfigurationObject() throws LpException {
        for (Field field : component.getClass().getFields()) {
            Component.ContainsConfiguration annotation =
                    field.getAnnotation(Component.ContainsConfiguration.class);
            if (annotation == null) {
                continue;
            }
            try {
                return field.get(component);
            } catch (IllegalAccessException ex) {
                throw new LpException("Can't get runtime configuration.", ex);
            }
        }
        return null;
    }

}
