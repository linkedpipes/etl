package com.linkedpipes.executor.execution.contoller;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;
import com.linkedpipes.executor.execution.entity.event.ComponentBeginImpl;
import com.linkedpipes.executor.execution.entity.event.ComponentFinishedImpl;
import com.linkedpipes.executor.execution.entity.event.ExecutionFailed;
import com.linkedpipes.executor.logging.boundary.MdcValue;
import com.linkedpipes.executor.rdf.boundary.DefinitionStorage;
import com.linkedpipes.etl.executor.api.v1.context.ExecutionContext;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration.Component.ExecutionType;
import com.linkedpipes.executor.execution.entity.event.ComponentFailedImpl;
import java.io.File;

/**
 * Execute single component. The {@link #isTerminationFlag()} should be used to verify proper thread termination.
 *
 * In case of failure failure message is broadcasted.
 *
 * @author Škoda Petr
 */
class ComponentExecutor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentExecutor.class);

    private final ExecutionContext context;

    /**
     * Contains pipeline definition.
     */
    private final DefinitionStorage definitionDataUnit;

    /**
     * Instance of component to execute.
     */
    private final Component componentInstance;

    /**
     * Component definition.
     */
    private final PipelineConfiguration.Component component;

    /**
     * Available instances of data units.
     */
    private final Map<String, ManagableDataUnit> dataUnitInstances;

    /**
     * This flag is set at the end of {@link #run()} method to indicate that the thread has not been killed by error or
     * an exception.
     */
    private boolean terminationFlag = false;

    ComponentExecutor(ExecutionContext context, DefinitionStorage definitionDataUnit,
            Component componentInstance, PipelineConfiguration.Component component,
            Map<String, ManagableDataUnit> dataUnitInstances) {
        this.context = context;
        this.definitionDataUnit = definitionDataUnit;
        this.componentInstance = componentInstance;
        this.component = component;
        this.dataUnitInstances = dataUnitInstances;
    }

    @Override
    public void run() {
        // Gather data units that will be acessible for the component.
        final Map<String, DataUnit> usedDataUnitInstances = new HashMap<>(component.getDataUnits().size() + 1);
        usedDataUnitInstances.put(definitionDataUnit.getResourceUri(), definitionDataUnit);
        // Prepare data units for the component.
        for (PipelineConfiguration.DataUnit dataUnit : component.getDataUnits()) {
            final ManagableDataUnit managableDataUnitInstace = dataUnitInstances.get(dataUnit.getUri());
            try {
                // We trust ManagableDataUnit and provide it with all data units. As there should be no
                // dependecy between single Componenet data units, so every data unit should have
                // all dependencies initialized.
                if (dataUnit.getSource() == null) {
                    // Load from other data units.
                    LOG.debug("Initializing data unit: {} : {}", dataUnit.getName(), dataUnit.getUri());
                    managableDataUnitInstace.initialize(dataUnitInstances);
                } else {
                    LOG.debug("Initializing data unit: {} : {}", dataUnit.getName(), dataUnit.getUri());
                    managableDataUnitInstace.initialize(new File(dataUnit.getSource().getPath()));
                }
            } catch (ManagableDataUnit.DataUnitException ex) {
                context.sendMessage(ExecutionFailed.executionFailed("Can't prepare data units!", ex));
                terminationFlag = true;
                return;
            } catch (Throwable t) {
                context.sendMessage(ExecutionFailed.executionFailed("Can't initialize component!", t));
                terminationFlag = true;
                return;
            }
            usedDataUnitInstances.put(managableDataUnitInstace.getResourceUri(), managableDataUnitInstace);
        }
        //
        if (component.getExecutionType() == ExecutionType.MAPPED) {
            LOG.info("Execution skipped as the component is marked as 'mapped'.");
            terminationFlag = true;
            return;
        }
        // Prepare component.
        LOG.info("Preparing component ...");
        try {
            // DPU gets only some data units.
            componentInstance.initialize(usedDataUnitInstances, context);
        } catch (Component.InitializationFailed ex) {
            context.sendMessage(ExecutionFailed.executionFailed("Can't initialize component!", ex));
            terminationFlag = true;
            return;
        } catch (Throwable t) {
            context.sendMessage(ExecutionFailed.executionFailed("Can't initialize component!", t));
            terminationFlag = true;
            return;
        }
        LOG.info("Preparing component ... done");
        // Execute main component method.
        LOG.info("Executing component ...");
        context.sendMessage(new ComponentBeginImpl(component));
        MDC.put(MdcValue.COMPONENT_FLAG, null);
        try {
            componentInstance.execute(context);
            context.sendMessage(new ComponentFinishedImpl(component));
        } catch (com.linkedpipes.etl.executor.api.v1.component.Component.ComponentFailed ex) {
            context.sendMessage(new ComponentFailedImpl(component));
            context.sendMessage(ExecutionFailed.executionFailed("Component execution failed!", ex));
        } catch (Throwable ex) {
            context.sendMessage(new ComponentFailedImpl(component));
            context.sendMessage(ExecutionFailed.executionFailed("Component execution failed on Throwable!", ex));
        }
        LOG.info("Executing component ... done");
        MDC.remove(MdcValue.COMPONENT_FLAG);

        terminationFlag = true;
    }

    /**
     *
     * @return After execution return true if thread has not been killed by {@link Throwable}.
     */
    public boolean isTerminationFlag() {
        return terminationFlag;
    }

}
