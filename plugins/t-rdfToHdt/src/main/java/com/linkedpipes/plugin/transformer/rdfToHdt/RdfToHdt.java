package com.linkedpipes.plugin.transformer.rdfToHdt;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;

import java.io.File;
import java.io.IOException;

public final class RdfToHdt implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.OutputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public RdfToHdtConfiguration configuration;

    @Override
    public void execute() throws LpException {
        checkConfiguration();
        HDT hdt = createHdt();
        saveHdt(hdt);
    }

    private void checkConfiguration() throws LpException {
        if (nullOrEmpty(configuration.getFileName())) {
            throw new LpException("Invalid output file name.");
        }
        if (nullOrEmpty(configuration.getBaseIri())) {
            throw new LpException("Invalid base IRI.");
        }
    }

    private boolean nullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    private HDT createHdt() throws LpException {
        try (HdtTripleIterator iterator = new HdtTripleIterator(inputRdf)) {
            return HDTManager.generateHDT(
                    iterator,
                    configuration.getBaseIri(),
                    new HDTSpecification(),
                    null);
        } catch (IOException | ParserException ex) {
            throw new LpException("Can't convert RDF to HDT.", ex);
        }
    }

    private void saveHdt(HDT hdt) throws LpException {
        File outputFile = outputFiles.createFile(configuration.getFileName());
        try {
            hdt.saveToHDT(outputFile.toString(), null);
        } catch (IOException ex) {
            throw new LpException("Can't save HDT.", ex);
        }
    }

}
