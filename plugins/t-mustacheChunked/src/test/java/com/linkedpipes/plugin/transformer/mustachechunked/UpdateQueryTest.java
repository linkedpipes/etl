package com.linkedpipes.plugin.transformer.mustachechunked;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class UpdateQueryTest {

    @Test
    public void test_simple_replace_0() {
        final String input = "{{! PREFIX : <http://default/> }}\n"
                + "{{!PREFIX local : <http://localhost/local/>}}\n"
                + "{{:name}}\n"
                + "{{local:name}}";
        final String expected = "\n\n"
                + "{{http://default/name}}\n"
                + "{{http://localhost/local/name}}";
        Assertions.assertEquals(
                expected, MustacheTemplatePrefixExpander.expand(input));
    }

    @Test
    public void test_simple_replace_1() {
        final String input = "{{!PREFIX local : <http://localhost/local/>\n"
                + "PREFIX : <http://default/>}}\n"
                + "{{:name}}\n"
                + "{{local:name}}";
        final String expected = "\n"
                + "{{http://default/name}}\n"
                + "{{http://localhost/local/name}}";
        Assertions.assertEquals(
                expected, MustacheTemplatePrefixExpander.expand(input));
    }

    @Test
    public void test_simple_replace_2() {
        final String input = "{{!\n"
                + "\n"
                + "PREFIX local : <http://localhost/local/>\n"
                + "    PREFIX : <http://default/>\n"
                + "\n"
                + "}}\n"
                + "{{:name}}\n"
                + "{{local:name}}";
        final String expected = "\n"
                + "{{http://default/name}}\n"
                + "{{http://localhost/local/name}}";
        Assertions.assertEquals(
                expected, MustacheTemplatePrefixExpander.expand(input));
    }

    @Test
    public void test_simple_replace_3() {
        final String input = "{{!PREFIX local: <http://localhost/local/>"
                + "PREFIX : <http://default/>}}\n"
                + "{{:name}}\n"
                + "{{local:name}}";
        final String expected = "\n"
                + "{{http://default/name}}\n"
                + "{{http://localhost/local/name}}";
        Assertions.assertEquals(
                expected, MustacheTemplatePrefixExpander.expand(input));
    }

    @Test
    public void test_simple_replace_4() {
        final String input = "{{!PREFIX local : <http://localhost/local/>}}  "
                + "{{!PREFIX : <http://default/>}}\n"
                + "{{:name}}\n"
                + "{{local:name}}"
                + "{{noPrefix}}";
        final String expected = "  \n"
                + "{{http://default/name}}\n"
                + "{{http://localhost/local/name}}{{noPrefix}}";
        Assertions.assertEquals(
                expected, MustacheTemplatePrefixExpander.expand(input));
    }

}
