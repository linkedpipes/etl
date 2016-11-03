package com.linkedpipes.plugin.transformer.mustache;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals(expected, UpdateQuery.expandPrefixes(input));
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
        Assert.assertEquals(expected, UpdateQuery.expandPrefixes(input));
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
        Assert.assertEquals(expected, UpdateQuery.expandPrefixes(input));
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
        Assert.assertEquals(expected, UpdateQuery.expandPrefixes(input));
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
        Assert.assertEquals(expected, UpdateQuery.expandPrefixes(input));
    }

}
