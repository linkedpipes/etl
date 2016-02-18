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
import com.linkedpipes.executor.execution.entity.event.ComponentEndImpl;
import com.linkedpipes.executor.execution.entity.event.ExecutionFailed;
import com.linkedpipes.executor.logging.boundary.MdcValue;
import com.linkedpipes.executor.rdf.boundary.DefinitionStorage;
import com.linkedpipes.etl.executor.api.v1.context.ExecutionContext;

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

    public ComponentExecutor(ExecutionContext context, DefinitionStorage definitionDataUnit,
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
        // Prepare data units.
        for (PipelineConfiguration.DataUnit dataUnit : component.getDataUnits()) {
            final ManagableDataUnit managableDataUnitInstace = dataUnitInstances.get(dataUnit.getUri());
            try {
                // We trust ManagableDataUnit and provide it with all data units. As there should be no
                // dependecy between single Componenet data units, so every data unit should have
                // all dependencies initialized.
                managableDataUnitInstace.initialize(dataUnitInstances);
            } catch (ManagableDataUnit.DataUnitException ex) {
                context.sendMessage(ExecutionFailed.executionFailed("Can't prepare data units!", ex));
                terminationFlag = true;
                return;
            }catch (Throwable t) {
                context.sendMessage(ExecutionFailed.executionFailed("Can't initialize component!", t));
                terminationFlag = true;
                return;
            }
            usedDataUnitInstances.put(managableDataUnitInstace.getResourceUri(), managableDataUnitInstace);
        }
        // Prepare componenet.
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
        } catch (com.linkedpipes.etl.executor.api.v1.component.Component.ComponentFailed ex) {
            context.sendMessage(ExecutionFailed.executionFailed("Component execution failed!", ex));
        } catch (Throwable ex) {
            context.sendMessage(ExecutionFailed.executionFailed("Component execution failed on Throwable!", ex));
        }
        LOG.info("Executing component ... done");
        MDC.remove(MdcValue.COMPONENT_FLAG);
        context.sendMessage(new ComponentEndImpl(component));
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
