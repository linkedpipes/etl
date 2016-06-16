package com.linkedpipes.etl.executor.api.v1.exception;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Petr Škoda
 */
public class ExceptionTest {

    @Test
    public void oneLanguageMessage() {
        final List<LocalizedException.LocalizedString> messages
                = Arrays.asList(new LocalizedException.LocalizedString(
                        "Value: {} {}", "en"));
        final LocalizedException ex = new LocalizedException(
                messages, 12, "text");
        //
        Assert.assertEquals("Value: 12 text", ex.getMessage());
    }

    @Test
    public void oneLanguageMessageWithCause() {
        final List<LocalizedException.LocalizedString> messages
                = Arrays.asList(new LocalizedException.LocalizedString(
                        "Value: {} {}", "en"));
        final Exception rootCause = new Exception();
        final LocalizedException ex = new LocalizedException(
                messages, 12, "text", rootCause);
        //
        Assert.assertEquals("Value: 12 text", ex.getMessage());
        Assert.assertEquals(rootCause, ex.getCause());
    }

}
