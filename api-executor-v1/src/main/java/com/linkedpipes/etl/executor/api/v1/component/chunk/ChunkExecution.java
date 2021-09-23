package com.linkedpipes.etl.executor.api.v1.component.chunk;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class ChunkExecution<Chunk, Product>
        implements SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(ChunkExecution.class);

    @Component.Inject
    public ProgressReport progressReport;

    protected Component.Context context;

    protected Iterator<Chunk> chunkSource;

    protected boolean terminateExecution = false;

    protected Object lock = new Object();

    @Override
    public void execute(Component.Context context) throws LpException {
        this.context = context;
        chunkSource = chunks();
        ExecutorService executor = createExecutorService();
        progressReport.start(getChunkCount());
        List<ChunkTransformer<Chunk, Product>> executors = createExecutors();
        LOG.info("Using {} executors (threads)", executors.size());
        startExecutors(executor, executors);
        awaitTermination(executor);
        checkExecutors(executors);
        progressReport.done();
    }

    /**
     * Access to the iterator is synchronized.
     */
    protected abstract Iterator<Chunk> chunks() throws LpException;

    protected ExecutorService createExecutorService() {
        int threads = getThreadCount();
        return Executors.newFixedThreadPool(threads);
    }

    protected abstract int getThreadCount();

    protected abstract long getChunkCount();

    protected List<ChunkTransformer<Chunk, Product>> createExecutors() {
        List<ChunkTransformer<Chunk, Product>> result = new ArrayList<>();
        int threadCount = getThreadCount();
        for (int i = 0; i < threadCount; ++i) {
            ChunkTransformer<Chunk, Product> executor = createExecutor();
            result.add(executor);
        }
        return result;
    }

    protected abstract ChunkTransformer<Chunk, Product> createExecutor();

    protected void startExecutors(
            ExecutorService executor,
            List<ChunkTransformer<Chunk, Product>> executors) {
        for (ChunkTransformer<Chunk, Product> constructExecutor : executors) {
            executor.submit(constructExecutor);
        }
    }

    protected void awaitTermination(ExecutorService executor) {
        executor.shutdown();
        LOG.info("Waiting for executors to finish ...");
        while (true) {
            try {
                if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }
        LOG.info("Waiting for executors to finish ... done");
    }

    protected void checkExecutors(List<ChunkTransformer<Chunk, Product>> executors)
            throws LpException {
        if (terminateExecution) {
            throw new LpException("At least chunk execution failed.");
        }
        for (ChunkTransformer<Chunk, Product> executor : executors) {
            if (!executor.isFinished()) {
                throw new LpException("At least executor was terminated.");
            }
        }
    }

    public void terminate() {
        if (shouldSkipFailures()) {
            return;
        }
        terminateExecution = true;
    }

    protected abstract boolean shouldSkipFailures();

    public Chunk next() {
        if (context.isCancelled() || terminateExecution) {
            return null;
        }
        synchronized (lock) {
            if (chunkSource.hasNext()) {
                return chunkSource.next();
            } else {
                return null;
            }
        }
    }

    public synchronized void submit(Product product) throws LpException {
        synchronized (lock) {
            try {
                submitInternal(product);
            } catch (LpException ex) {
                throw new LpException("Can't submit chunk results.", ex);
            }
            progressReport.entryProcessed();
        }
    }

    /**
     * Do not call this method directly.
     *
     * @param product Content of this object may change, later.
     */
    protected abstract void submitInternal(Product product) throws LpException;

}
