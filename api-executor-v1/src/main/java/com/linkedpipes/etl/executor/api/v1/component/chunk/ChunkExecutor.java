package com.linkedpipes.etl.executor.api.v1.component.chunk;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

public abstract class ChunkExecutor <Chunk, Product> implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(ChunkExecutor.class);

    protected final Map<String,String> contextMap = MDC.getCopyOfContextMap();

    private final ChunkExecution<Chunk, Product> owner;

    private boolean finished = false;

    public ChunkExecutor(ChunkExecution<Chunk, Product> owner) {
        this.owner = owner;
    }

    @Override
    public void run() {
        MDC.setContextMap(contextMap);
        LOG.info("Executor is running ...");
        while(true) {
            Chunk next = owner.next();
            if (next == null) {
                break;
            }
            try {
                Product result = processChunk(next);
                owner.submit(result);
            } catch (Throwable ex) {
                owner.terminate();
                LOG.error("Executor caught throwable.", ex);
            }
        }
        this.finished = true;
        LOG.info("Executor is running ... done");
    }

    protected abstract Product processChunk(Chunk chunk) throws LpException;

    public boolean isFinished() {
        return finished;
    }

}
