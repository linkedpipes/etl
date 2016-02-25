package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.rdf.RdfToPojo;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = RdfToFileVocabulary.CONFIG_CLASS)
public class RdfToFileConfiguration {

    @RdfToPojo.Property(uri = RdfToFileVocabulary.CONFIG_FILE_NAME)
    private String fileName;

    @RdfToPojo.Property(uri = RdfToFileVocabulary.CONFIG_FILE_TYPE)
    private String fileType;

    @RdfToPojo.Property(uri = RdfToFileVocabulary.CONFIG_GRAPH_URI)
    private String graphUri;

    public RdfToFileConfiguration() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getGraphUri() {
        return graphUri;
    }

    public void setGraphUri(String graphUri) {
        this.graphUri = graphUri;
    }

    public RDFFormat getFileFormat() throws DataProcessingUnit.ExecutionFailed {
        return Rio.getParserFormatForMIMEType(fileType).orElseThrow(() -> {
            return new DataProcessingUnit.ExecutionFailed("Invalid output file type: {1}", fileType);
        });
    }

}
