package com.linkedpipes.plugin.transformer.xslt;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import net.sf.saxon.s9api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

class XsltWorker implements Callable<Object> {

    /**
     * Definition of XSLT parameter.
     */
    public static class Parameter {

        private final String key;

        private final String value;

        public Parameter(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Definition of a task.
     */
    public static class Payload {

        /**
         * Reference to the input entry.
         */
        public FilesDataUnit.Entry entry;

        public File output;

        public final List<Parameter> parameter = new LinkedList<>();

    }

    private static final Logger LOG = LoggerFactory.getLogger(XsltWorker.class);

    private final ConcurrentLinkedQueue<Payload> workQueue;

    private final ConcurrentLinkedQueue<Exception> exceptions;

    private final AtomicInteger counter;

    private XsltTransformer transformer;

    private boolean isSkipOnError;

    private final long workSize;

    private ProgressReport progressReport;

    public XsltWorker(ConcurrentLinkedQueue<Payload> workQueue,
            ConcurrentLinkedQueue<Exception> exceptions,
            AtomicInteger counter, boolean isSkipOnError,
            long workSize, ProgressReport progressReport) {
        this.workQueue = workQueue;
        this.exceptions = exceptions;
        this.counter = counter;
        this.isSkipOnError = isSkipOnError;
        this.workSize = workSize;
        this.progressReport = progressReport;
    }

    public void initialize(String template) throws SaxonApiException {
        final Processor processor = new Processor(false);
        processor.registerExtensionFunction(UUIDGenerator.getInstance());

        final XsltCompiler compiler = processor.newXsltCompiler();
        final XsltExecutable executable = compiler.compile(new StreamSource(
                new StringReader(template)));
        transformer = executable.load();
    }

    @Override
    public Object call() {
        Payload payload;
        while ((payload = workQueue.poll()) != null) {
            // Check exceptions.
            if (!isSkipOnError && !exceptions.isEmpty()) {
                break;
            }
            // Prepare parameters.
            transformer.clearParameters();
            for (Parameter parameter : payload.parameter) {
                transformer.setParameter(new QName(parameter.key),
                        new XdmAtomicValue(parameter.value));
            }
            //
            LOG.debug("Transforming: {}/{} : {}",
                    counter.getAndIncrement(), workSize,
                    payload.entry.getFileName());
            boolean deleteFile = false;
            final Serializer output = new Serializer(payload.output);
            try {
                transformer.setSource(new StreamSource(payload.entry.toFile()));
                transformer.setDestination(output);
                transformer.transform();
            } catch (SaxonApiException ex) {
                LOG.error("Can't transform file: {}",
                        payload.entry.getFileName(), ex);
                exceptions.add(ex);
                deleteFile = true;
            } finally {
                // Clear document cache.
                try {
                    output.close();
                } catch (SaxonApiException ex) {
                    LOG.warn("Can't close output.", ex);
                }
                transformer.getUnderlyingController().clearDocumentPool();
            }
            // If the transformation fail, we may end up with incomplete
            // output file. So we delete it if necessary.
            if (deleteFile) {
                payload.output.delete();
            }
            progressReport.entryProcessed();
        }
        // Close the transformer.
        try {
            transformer.close();
        } catch (SaxonApiException ex) {
            LOG.warn("Can't close transformer.", ex);
        }
        return null;
    }

}
