package com.linkedpipes.etl.storage.pipeline;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represent a pipeline object.
 */
public class PipelineRef {

    public static final IRI TYPE;

    public static final IRI HAS_VERSION;

    /**
     * Current pipeline version.
     */
    public static final int VERSION_NUMBER = 2;

    static {
        TYPE = SimpleValueFactory.getInstance().createIRI(
                "http://linkedpipes.com/ontology/Pipeline");
        HAS_VERSION = SimpleValueFactory.getInstance().createIRI(
                "http://etl.linkedpipes.com/ontology/version");
    }

    /**
     * Path to the file that contains the pipeline.
     */
    private final File file;

    /**
     * Reference to the pipeline info.
     */
    private PipelineInfo info;

    /**
     * RDF reference to this pipeline. This can be used to provide a client
     * with reference to the pipeline where the complete
     * pipeline definition is not required.
     */
    private Collection<Statement> referenceRdf = new ArrayList<>();

    PipelineRef(File file, PipelineInfo info) {
        this.file = file;
        this.info = info;
    }

    public File getFile() {
        return file;
    }

    /**
     * Can return Null if no information has been loaded.
     */
    public PipelineInfo getInfo() {
        return info;
    }

    public void setInfo(PipelineInfo info) {
        this.info = info;
    }

    /**
     * Return Null if pipeline information has not yet been loaded.
     */
    public String getIri() {
        if (info == null) {
            return null;
        } else {
            return info.getIri();
        }
    }

    public Collection<Statement> getReferenceRdf() {
        return referenceRdf;
    }

    public void setReferenceRdf(Collection<Statement> referenceRdf) {
        this.referenceRdf = referenceRdf;
    }

}
