package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.ClosableRdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class PipelineModelTest {

    @Test
    public void linearConnectionTest() throws Exception {
        PipelineModel model = loadModel("pipeline/linear_connection.trig");

        Assert.assertEquals(
                model.getPortGroup("http://localhost/output_port"),
                model.getPortGroup("http://localhost/input_port"));

        Assert.assertEquals(0,
                model.getPortSources("http://localhost/output_port").size());
        Assert.assertEquals(1,
                model.getPortSources("http://localhost/input_port").size());

        Assert.assertEquals(2,
                model.getPortTypes("http://localhost/input_port").size());

        Assert.assertTrue(
                model.getPortTypes("http://localhost/input_port").contains(
                        "http://linkedpipes.com/ontology/dataUnit/sesame/1.0/rdf/SingleGraph"
                ));
    }

    @Test
    public void distinctGroupsTest() throws Exception {
        PipelineModel model = loadModel("pipeline/distinct_groups.trig");

        Assert.assertEquals(
                model.getPortGroup("http://localhost/output_port_1"),
                model.getPortGroup("http://localhost/input_port_1"));

        Assert.assertEquals(
                model.getPortGroup("http://localhost/output_port_2"),
                model.getPortGroup("http://localhost/input_port_2"));

        Assert.assertNotEquals(
                model.getPortGroup("http://localhost/output_port_1"),
                model.getPortGroup("http://localhost/input_port_2"));

        Assert.assertTrue(
                model.getPortGroup("http://localhost/output_port_1") >= 0);
        Assert.assertTrue(
                model.getPortGroup("http://localhost/input_port_2") >= 0);
    }

    @Test
    public void complexGroupsTest() throws Exception {
        PipelineModel model = loadModel("pipeline/complex_group.trig");

        Assert.assertEquals(
                model.getPortGroup("http://localhost/port_1"),
                model.getPortGroup("http://localhost/port_2"));

        Assert.assertEquals(
                model.getPortGroup("http://localhost/port_2"),
                model.getPortGroup("http://localhost/port_3"));

        Assert.assertEquals(
                model.getPortGroup("http://localhost/port_3"),
                model.getPortGroup("http://localhost/port_4"));

        Assert.assertEquals(4, model.getIrisOfPortsInGroup(
                model.getPortGroup("http://localhost/port_3")).size());
    }

    @Test
    public void missingPorts() throws Exception {
        PipelineModel model = loadModel("pipeline/linear_connection.trig");
        try {
            model.getPortGroup("http://localhost/missing");
            Assert.fail();
        } catch (MissingPortException ex) {

        }
        try {
            model.getPortSources("http://localhost/missing");
            Assert.fail();
        } catch (MissingPortException ex) {

        }
        try {
            model.getPortTypes("http://localhost/missing");
            Assert.fail();
        } catch (MissingPortException ex) {

        }
    }

    @Test
    public void missingProfileUseDefault() throws Exception {
        PipelineModel model = loadModel("pipeline/missing_profile.trig");

        Assert.assertEquals(
                model.getPortGroup("http://localhost/output_port_1"),
                model.getPortGroup("http://localhost/input_port_1"));

        Assert.assertEquals(
                model.getPortGroup("http://localhost/output_port_2"),
                model.getPortGroup("http://localhost/input_port_2"));

        Assert.assertEquals(
                model.getPortGroup("http://localhost/output_port_1"),
                model.getPortGroup("http://localhost/input_port_2"));

    }

    @Test
    public void singleGraphTest() throws Exception {
        PipelineModel model = loadModel("pipeline/single_graph.trig");

        Assert.assertEquals(
                model.getPortGroup("http://localhost/output_port_1"),
                model.getPortGroup("http://localhost/input_port_1"));

        Assert.assertEquals(
                model.getPortGroup("http://localhost/output_port_2"),
                model.getPortGroup("http://localhost/input_port_2"));

        Assert.assertEquals(
                model.getPortGroup("http://localhost/output_port_1"),
                model.getPortGroup("http://localhost/input_port_2"));
    }

    private PipelineModel loadModel(String fileName)
            throws IOException, RdfUtilsException {
        ClosableRdfSource source = Rdf4jUtils.loadAsSource(fileName);
        try {
            PipelineModelFactory factory = new PipelineModelFactory();
            return factory.createModel(source,
                    "http://localhost/pipeline",
                    "http://localhost/graph");
        } finally {
            source.close();
        }
    }

}
