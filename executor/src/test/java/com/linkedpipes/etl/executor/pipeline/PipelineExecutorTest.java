package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.ConfigurationHolder;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.RuntimeConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.plugin.PluginServiceHolder;
import com.linkedpipes.etl.executor.plugin.v1.PluginV1Instance;
import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineExecutorTest {

    /**
     * Dummy component, that does nothing.
     */
    public class DummyComponent extends PluginV1Instance {

        public DummyComponent() {
            super(null, null, null);
        }

        @Override
        public void initialize(Map<String, DataUnit> dataUnits, Component.Context context) {
            LOG.info("bindToPipeline");
        }

        @Override
        public void loadConfiguration(RdfSource definition) {
            LOG.info("loadConfiguration");
        }

        @Override
        public RuntimeConfiguration getRuntimeConfiguration() {
            // No runtime configuration.
            return null;
        }

        @Override
        public void execute(Component.Context context) {
            LOG.info("execute");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(PipelineExecutorTest.class);

    // TODO Re-enable: Pipeline.load() now always unpacks a raw (Designer-style,
    // template-referencing) pipeline instead of loading an already-resolved
    // one directly, so definition.trig here would need to become a raw
    // pipeline-with-templates fixture (see pipeline-with-templates.jsonld)
    // for this scenario to be valid again.
    public void executeTwoConnectedComponents() throws Exception {
        // Prepare working directory.
        File file = new File(Thread.currentThread()
                .getContextClassLoader()
                .getResource("pipeline/two-connected-components.trig")
                .getPath());
        File directory = Files.createTempDirectory("lp-test-executor-exec-").toFile();
        (new File(directory, "definition")).mkdirs();
        Files.copy(file.toPath(), (new File(directory, "definition/definition.trig")).toPath());
        //
        PluginServiceHolder moduleFacade = Mockito.mock(PluginServiceHolder.class);
        Mockito.when(moduleFacade.getComponent(Mockito.any(), Mockito.eq("http://pipeline/component/1")))
                .thenReturn(new DummyComponent());
        Mockito.when(moduleFacade.getComponent(Mockito.any(), Mockito.eq("http://pipeline/component/2")))
                .thenReturn(new DummyComponent());
        ConfigurationHolder configuration = Mockito.mock(ConfigurationHolder.class);
        PipelineExecutor executor = new PipelineExecutor(directory, "http://execution", moduleFacade, configuration);
        executor.execute();
        FileUtils.deleteDirectory(directory);
    }
}
