package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultServiceFactoryTest {

    @Test
    public void exceptionFactory() throws LpException {
        final DefaultServiceFactory factory = new DefaultServiceFactory();
        final ExceptionFactory result = (ExceptionFactory) factory.create(
                ExceptionFactory.class, null, null, null);
        Assert.assertNotNull(result);
        //
        Assert.assertNotNull(result.failure("Message"));
    }

    @Test
    public void progressReport() throws LpException {
        final Component.Context context = Mockito.mock(Component.Context.class);
        final DefaultServiceFactory factory = new DefaultServiceFactory();
        final ProgressReport result = (ProgressReport) factory.create(
                ProgressReport.class, null, null, context);
        Assert.assertNotNull(result);
        //
        result.start(2);
        result.entryProcessed();
        result.entryProcessed();
        result.done();
        //
        Mockito.verify(context, Mockito.times(4)).sendMessage(Mockito.any());
    }

}
