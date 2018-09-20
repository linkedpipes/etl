package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import org.junit.Test;
import org.mockito.Mockito;

public class MapComponentTest {

    @Test
    public void execute() throws ExecutorException {
        final ExecutionObserver execution = Mockito.mock(ExecutionObserver.class);
        final ExecutionComponent execComponent =
                Mockito.mock(ExecutionComponent.class);
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
