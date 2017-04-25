package com.linkedpipes.plugin.transformer.xsparql;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourceforge.xsparql.Main;
import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.sourceforge.xsparql.evaluator.XSPARQLEvaluator;
import org.sourceforge.xsparql.rewriter.Helper;
import org.sourceforge.xsparql.rewriter.XSPARQLProcessor;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by patzo on 11.04.17.
 */
public class XsparqlPlugin implements Component, SequentialExecution  {
    private static final Logger LOG = LoggerFactory.getLogger(XsparqlPlugin.class);
    private final XSPARQLProcessor proc = new XSPARQLProcessor();
    private final XSPARQLEvaluator xe = new XSPARQLEvaluator();
    private boolean parseErrors = false;
    private int numOfSyntaxErrors;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "Parameters")
    public SingleGraphDataUnit parametersRdf;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public XsparqlConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        LOG.debug("Executing XSPARQL");
        String[] args = new String[3];
        progressReport.start(inputFiles.size());
        LOG.debug(args.toString());

        tieSystemOutAndErrToLog();

        for (FilesDataUnit.Entry entry : inputFiles) {
            LOG.debug("Processing file: " + entry.getFileName());
            LOG.debug("Output write dir: " + outputFiles.getWriteDirectory().toString());
            String outputFilename = entry.getFileName().substring(0, entry.getFileName()
                    .lastIndexOf('.')) + "." + configuration.getNewExtension();
            File outputFile = outputFiles.createFile(outputFilename);

            try (PrintStream outputStream = new PrintStream(
                    new FileOutputStream(outputFile), false, "UTF-8")) {

                try {
                    // Set the external variables for Saxon
                    xe.setXqueryExternalVars(getExternalVariables(entry));

                    // Rewrite and process query
                    String xquery = rewriteQuery(new StringReader(configuration.getXsparqlQuery())
                            , entry.getFileName());
                    postProcessing(xquery, outputStream);
                } catch (FileNotFoundException e) {
                    String filename = entry.getFileName();
                    LOG.error("File not found: " + filename);
                } catch (Exception e) {
                    LOG.error("Error executing query ("+entry.getFileName()+"): "+e.getMessage());
                }
                String outputPath = outputFiles.getWriteDirectory().toString() + File.separator + outputFilename;
                LOG.debug("Output file name: " + (new File(outputPath)).getAbsolutePath());
                LOG.debug("Output file lenght: " + (new File(outputPath)).length());
                if ((new File(outputPath)).length() == 0)
                    throw exceptionFactory.failure("Error in XSPARQL tranformator. ");

                progressReport.entryProcessed();

            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't write output to file.",
                        ex);
            }
        }
        progressReport.done();
    }

    private String rewriteQuery(Reader is, String filename) {
        String xquery = null;
        try {
            proc.setQueryFilename(filename);

            xquery = proc.process(is);
            numOfSyntaxErrors = proc.getNumberOfSyntaxErrors();

        } catch (Exception e) {
            System.err.println("Parse error: " + e);
            e.printStackTrace();
            parseErrors = true;
        }
        return xquery;
    }

    private void postProcessing(final String xquery, PrintStream outputStream) throws Exception {
        parseErrors = parseErrors || numOfSyntaxErrors > 0;

        if (parseErrors) {
            return;
        }

        String result;
        xe.setDBconnection(proc.getDBconnection());
        xe.evaluateRewrittenQuery(new StringReader(xquery), new PrintWriter(outputStream));
        //outputStream.print(result);

    }

    private Map<String, String> getExternalVariables(FilesDataUnit.Entry entry) {
        final Map<String, String> externalVariables = new HashMap<String, String>();
        String dir = inputFiles.getReadDirectories().toArray()[1].toString();
        externalVariables.put("input", dir + File.separator + entry.getFileName());
        LOG.debug("Number of external variables: " + configuration.getReferences().size());
        for (XsparqlConfiguration.Reference var : configuration.getReferences()) {
            LOG.debug("Binding variable: " + var.getKey() + " with value: " + var.getVal());
            externalVariables.put(var.getKey(), var.getVal());
        }
        return externalVariables;
    }

    public static void tieSystemOutAndErrToLog() {
        System.setOut(createLoggingProxy(System.out));
        System.setErr(createLoggingProxy(System.err));
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            public void print(final String string) {
                realPrintStream.print(string);
                LOG.info(string);
            }
        };
    }
}
