package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.Execution;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import org.junit.Test;
import org.mockito.Mockito;

public class MapComponentTest {

    @Test
    public void execute() throws ExecutorException {
        final Execution execution = Mockito.mock(Execution.class);
        final ExecutionModel.Component execComponent =
                Mockito.mock(ExecutionModel.Component.class);
        final MapComponent executor = new MapComponent(execution, execComponent);
        final DataUnitManager dataUnits = Mockito.mock(DataUnitManager.class);
        executor.execute(dataUnits);
        //
        Mockito.verify(dataUnits, Mockito.times(1))
                .onComponentMapByReference(execComponent);
        Mockito.verify(execution, Mockito.times(1))
                .onMapComponentBegin(execComponent);
        Mockito.verify(execution, Mockito.times(1))
                .onMapComponentSuccessful(execComponent);
    }

}
