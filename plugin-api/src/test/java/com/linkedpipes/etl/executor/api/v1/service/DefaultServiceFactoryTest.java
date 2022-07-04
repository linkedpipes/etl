package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DefaultServiceFactoryTest {

    @Test
    public void progressReport() throws LpException {
        Component.Context context = Mockito.mock(Component.Context.class);
        DefaultServiceFactory factory = new DefaultServiceFactory();
        ProgressReport result = (ProgressReport) factory.create(
                ProgressReport.class, null, null, context);
        Assertions.assertNotNull(result);
        //
        result.start(2);
        result.entryProcessed();
        result.entryProcessed();
        result.done();
        //
        Mockito.verify(context, Mockito.times(4)).sendMessage(Mockito.any());
    }

}
