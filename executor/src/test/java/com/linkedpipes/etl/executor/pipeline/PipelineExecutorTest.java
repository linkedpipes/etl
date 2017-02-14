package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.ManageableComponent;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.RuntimeConfiguration;
import com.linkedpipes.etl.executor.module.ModuleFacade;
import com.linkedpipes.etl.rdf.utils.RdfSource;
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
        public void initialize(Map<String, DataUnit> dataUnits,
                Component.Context context) throws LpException {
            LOG.info("bindToPipeline");
        }

        @Override
        public void loadConfiguration(String graph, RdfSource definition)
                throws LpException {
            LOG.info("loadConfiguration {}", graph);
        }

        @Override
        public RuntimeConfiguration getRuntimeConfiguration()
                throws LpException {
            // No runtime configuration.
            return null;
        }

        @Override
        public void execute() throws LpException {
            LOG.info("execute");
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(PipelineExecutorTest.class);

//    @Test
    public void executeTwoConnectedComponents() throws Exception {
        // Prepare working directory.
        final File file = new File(Thread.currentThread()
                .getContextClassLoader()
                .getResource("pipeline/twoConnectedComponents.trig")
                .getPath());
        final File directory =
                Files.createTempDirectory("lp-test-executor-exec-").toFile();
        (new File(directory, "definition")).mkdirs();
        Files.copy(file.toPath(),
                (new File(directory, "definition/definition.trig")).toPath());
        //
        final ModuleFacade moduleFacade = Mockito.mock(ModuleFacade.class);
        Mockito.when(moduleFacade.getComponent(Mockito.any(),
                Mockito.eq("http://pipeline/component/1")))
                .thenReturn(new DummyComponent());
        Mockito.when(moduleFacade.getComponent(Mockito.any(),
                Mockito.eq("http://pipeline/component/2")))
                .thenReturn(new DummyComponent());
        final PipelineExecutor executor =
                new PipelineExecutor(directory, "http://execution",
                        moduleFacade);
        executor.execute();
        FileUtils.deleteDirectory(directory);
    }

}
