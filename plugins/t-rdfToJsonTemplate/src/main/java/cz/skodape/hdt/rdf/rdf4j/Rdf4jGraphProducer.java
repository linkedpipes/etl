package cz.skodape.hdt.rdf.rdf4j;

import cz.skodape.hdt.core.OperationFailed;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

class Rdf4jGraphProducer implements RDFHandler, Runnable {

    public static class Container {

        public final List<Statement> statements;

        public final Resource graph;

        private Container(List<Statement> statements, Resource graph) {
            this.statements = statements;
            this.graph = graph;
        }

        public static Container copyAndWrap(
                List<Statement> statements, Resource graph) {
            return new Container(new ArrayList<>(statements), graph);
        }

        public static Container deadPill() {
            return new Container(null, null);
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(Rdf4jGraphProducer.class);

    private final File file;

    private final RDFFormat format;

    private Resource currentGraph = null;

    private final List<Statement> statements = new ArrayList<>();

    private final BlockingQueue<Container> queue = new LinkedBlockingDeque<>(4);

    public Rdf4jGraphProducer(File file) throws OperationFailed {
        this.file = file;
        this.format = getInputFileFormat();
    }

    protected RDFFormat getInputFileFormat() throws OperationFailed {
        Optional<RDFFormat> format =
                Rio.getParserFormatForFileName(file.getName());
        if (format.isEmpty()) {
            throw new OperationFailed("Can't determine file format.");
        }
        return format.get();
    }

    @Override
    public void run() {
        RDFParser parser = Rio.createParser(format);
        parser.setRDFHandler(this);
        try (InputStream inputStream = new FileInputStream(file)) {
            parser.parse(inputStream, "http://localhost/");
        } catch (IOException exception) {
            LOG.info("Can't parse input file.", exception);
        }
        try {
            LOG.info("Queue put pill ...");
            queue.put(Container.deadPill());
        } catch (InterruptedException exception) {
            LOG.info("Interrupted.");
        }
    }

    @Override
    public void startRDF() {
        // Do nothing.
    }

    @Override
    public void endRDF() {
        onNextGraphReady();
    }

    /**
     * Called when the content is ready.
     */
    private void onNextGraphReady() {
        if (statements.size() == 0) {
            return;
        }
        try {
            queue.put(Container.copyAndWrap(statements, currentGraph));
            statements.clear();
        } catch (InterruptedException exception) {
            LOG.info("Interrupted.");
        }
    }

    @Override
    public void handleNamespace(String prefix, String uri) {
        // Do nothing.
    }

    @Override
    public void handleStatement(Statement statement) {
        if (statement.getContext() == currentGraph) {
            statements.add(statement);
        } else {
            onNextGraphReady();
            currentGraph = statement.getContext();
            statements.add(statement);
        }
    }

    @Override
    public void handleComment(String comment) {
        // Do nothing.
    }

    public BlockingQueue<Container> getQueue() {
        return queue;
    }

}
