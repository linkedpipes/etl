package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.execution.Execution;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import org.junit.Test;
import org.mockito.Mockito;

public class SkipComponentTest {

    @Test
    public void execute() throws ExecutorException {
        final Execution execution = Mockito.mock(Execution.class);
        final Execution.Component execComponent =
                Mockito.mock(Execution.Component.class);
        final PipelineModel.Component pplComponent =
                Mockito.mock(PipelineModel.Component.class);
        Mockito.when(execution.getComponent(pplComponent))
                .thenReturn(execComponent);
        final SkipComponent executor =
                new SkipComponent(execution, pplComponent);
        executor.execute(null);
        //
        Mockito.verify(execution, Mockito.times(1)).
                onComponentSkipped(execComponent);
    }

}
