package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.RuntimeConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.service.ServiceFactory;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Implementation for default manageable wrap for sequential component.
 */
class SequentialWrap implements ManageableComponent, SequentialExecution {

    private final SequentialExecution component;

    private final ComponentInfo info;

    private final RdfSource definition;

    private final ServiceFactory serviceFactory;

    /**
     * @param component Must be instance of {@link SequentialExecution}.
     * @param info
     * @param definition
     */
    public SequentialWrap(SequentialExecution component, ComponentInfo info,
            RdfSource definition, ServiceFactory serviceFactory) {
        this.component = component;
        this.info = info;
        this.definition = definition;
        this.serviceFactory = serviceFactory;
    }

    @Override
    public void execute() throws LpException {
        try {
            component.execute();
        } catch (Throwable t) {
            throw new LpException("Execution failed.", t);
        }
    }

    @Override
    public void initialize(Map<String, DataUnit> dataUnits,
            Component.Context context) throws LpException {
        // Bind ports.
        bingPorts(dataUnits);
        // Inject services.
        injectServices(context);
    }

    @Override
    public void loadConfiguration(String graph, RdfSource definition)
            throws LpException {
        // Load configuration.
        for (Field field : component.getClass().getFields()) {
            if (field.getAnnotation(Component.Configuration.class) != null) {
                loadConfigurationForField(field, graph, definition);
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
     *
     * @param field
     * @param graph
     * @param definition
     */
    private void loadConfigurationForField(Field field, String graph,
            RdfSource definition) throws LpException {
        final Object instance;
        try {
            instance = field.getType().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new LpException("Can't create instance of {} for {}",
                    field.getType().getSimpleName(), field.getName(), ex);
        }
        //
        try {
            RdfUtils.loadByType(definition, graph,
                    instance, RdfToPojo.descriptorFactory());
        } catch (RdfUtilsException ex) {
            throw new LpException("Can't load for field: {}",
                    field.getName(), ex);
        }
        //
        try {
            field.set(component, instance);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new LpException("Can't set value to {}",
                    field.getName(), ex);
        }
    }

    /**
     * Bind all dataunit fields.
     *
     * @param dataUnits
     */
    private void bingPorts(Map<String, DataUnit> dataUnits) throws LpException {
        for (Field field : component.getClass().getFields()) {
            final Component.InputPort input =
                    field.getAnnotation(Component.InputPort.class);
            if (input != null) {
                bindPort(dataUnits, field, input.iri());
            }
            final Component.OutputPort output
                    = field.getAnnotation(Component.OutputPort.class);
            if (output != null) {
                bindPort(dataUnits, field, output.iri());
            }
        }
    }

    /**
     * Bind data unit for to given field.
     *
     * @param dataUnits
     * @param field
     * @param id
     */
    private void bindPort(Map<String, DataUnit> dataUnits, Field field,
            String id) throws LpException {
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
        //
        try {
            field.set(component, dataUnit);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new LpException("Can't set data unit: {}", id, ex);
        }
    }

    /**
     * Construct and inject services.
     *
     * @param context
     */
    private void injectServices(Component.Context context) throws LpException {
        for (Field field : component.getClass().getFields()) {
            if (field.getAnnotation(Component.Inject.class) == null) {
                continue;
            }
            final Object instance;
            try {
                instance = serviceFactory.create(field.getType(),
                        info.getIri(), info.getGraph(), definition, context);
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
            final Component.ContainsConfiguration annotation =
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
