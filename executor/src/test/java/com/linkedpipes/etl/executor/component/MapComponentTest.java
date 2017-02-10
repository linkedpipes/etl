package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.Execution;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import org.junit.Test;
import org.mockito.Mockito;

public class MapComponentTest {

    @Test
    public void execute() throws ExecutorException {
        final Execution execution = Mockito.mock(Execution.class);
        final Execution.Component execComponent =
                Mockito.mock(Execution.Component.class);
        final PipelineModel.Component pplComponent =
                Mockito.mock(PipelineModel.Component.class);
        Mockito.when(execution.getComponent(pplComponent))
                .thenReturn(execComponent);
        final MapComponent executor = new MapComponent(execution, pplComponent);
        final DataUnitManager dataUnits = Mockito.mock(DataUnitManager.class);
        executor.execute(dataUnits);
        //
        Mockito.verify(dataUnits, Mockito.times(1))
                .onComponentWillExecute(execComponent);
        Mockito.verify(dataUnits, Mockito.times(1))
                .onComponentDidExecute(execComponent);
        Mockito.verify(execution, Mockito.times(1))
                .onComponentMapped(execComponent);
    }

}
