package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.ManageableComponent;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.RuntimeConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.module.ModuleService;
import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

public class PipelineExecutorTest {

    /**
     * Dummy component, that does nothing.
     */
    public class DummyComponent implements ManageableComponent,
            SequentialExecution {

        @Override
        public void initialize(
                Map<String, DataUnit> dataUnits, Component.Context context) {
            LOG.info("bindToPipeline");
        }

        @Override
        public void loadConfiguration(RdfSource definition) {
            LOG.info("loadConfiguration {}");
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

    private static final Logger LOG =
            LoggerFactory.getLogger(PipelineExecutorTest.class);

    public void executeTwoConnectedComponents() throws Exception {
        // Prepare working directory.
        File file = new File(Thread.currentThread()
                .getContextClassLoader()
                .getResource("pipeline/two-connected-components.trig")
                .getPath());
        File directory =
                Files.createTempDirectory("lp-test-executor-exec-").toFile();
        (new File(directory, "definition")).mkdirs();
        Files.copy(file.toPath(),
                (new File(directory, "definition/definition.trig")).toPath());
        //
        ModuleService moduleFacade = Mockito.mock(ModuleService.class);
        Mockito.when(moduleFacade.getComponent(Mockito.any(),
                Mockito.eq("http://pipeline/component/1")))
                .thenReturn(new DummyComponent());
        Mockito.when(moduleFacade.getComponent(Mockito.any(),
                Mockito.eq("http://pipeline/component/2")))
                .thenReturn(new DummyComponent());
        PipelineExecutor executor =
                new PipelineExecutor(directory, "http://execution",
                        moduleFacade);
        executor.execute();
        FileUtils.deleteDirectory(directory);
    }

}
