package com.linkedpipes.plugin.transformer.tabular;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class UrlTemplateTest {

    @Test
    public void test_constant() throws InvalidTemplate {
        final StringTemplate template = new StringTemplate("http://localhost/value");
        template.initialize(null, Arrays.asList("col1", "col2", "col3"));
        Assertions.assertEquals("http://localhost/value", template.process(Arrays.asList("col1", "/value/", "s p a c e")));
    }

    @Test
    public void test_simple_expansion() throws InvalidTemplate {
        final StringTemplate template = new StringTemplate("http://localhost{col2}{col3}");
        template.initialize(null, Arrays.asList("col1", "col2", "col3"));
        Assertions.assertEquals("http://localhost%2Fvalue%2Fs+p+a+c+e", template.process(Arrays.asList("col1", "/value/", "s p a c e")));
    }

    @Test
    public void test_reserved_expansion() throws InvalidTemplate {
        final StringTemplate template = new StringTemplate("http://localhost{+col2}{+col3}");
        template.initialize(null, Arrays.asList("col1", "col2", "col3"));
        Assertions.assertEquals("http://localhost/value/s p a c e", template.process(Arrays.asList("col1", "/value/", "s p a c e")));
    }

    @Test
    public void test_fragment_expansion() throws InvalidTemplate {
        final StringTemplate template = new StringTemplate("http://localhost{+col2}{#col3}");
        template.initialize(null, Arrays.asList("col1", "col2", "col3"));
        Assertions.assertEquals("http://localhost/value#12", template.process(Arrays.asList("col1", "/value", "12")));
    }

}
