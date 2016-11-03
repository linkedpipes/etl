package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.SimpleValueFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represent a pipeline object.
 */
public class Pipeline {

    public static final IRI TYPE;

    public static final IRI HAS_VERSION;

    /**
     * Current pipeline version.
     */
    public static final int VERSION_NUMBER = 1;

    static {
        TYPE = SimpleValueFactory.getInstance().createIRI(
                "http://linkedpipes.com/ontology/Pipeline");
        HAS_VERSION = SimpleValueFactory.getInstance().createIRI(
                "http://etl.linkedpipes.com/ontology/version");
    }

    /**
     * Contains information about pipeline that are loaded from the
     * pipeline RDF definition.
     */
    public static class Info implements PojoLoader.Loadable {

        /**
         * IRI of the pipeline.
         */
        private String iri;

        /**
         * Pipeline version or 0 as default.
         */
        private int version = 0;

        /**
         * Labels.
         */
        private final List<Value> labels = new ArrayList<>(2);

        private final List<Value> tags = new ArrayList<>(4);

        Info() {
        }

        public String getIri() {
            return iri;
        }

        public int getVersion() {
            return version;
        }

        public List<Value> getLabels() {
            return Collections.unmodifiableList(labels);
        }

        public List<Value> getTags() {
            return tags;
        }

        @Override
        public void loadIri(String iri) {
            this.iri = iri;
        }

        @Override
        public PojoLoader.Loadable load(String predicate, Value value)
                throws PojoLoader.CantLoadException {
            switch (predicate) {
                case "http://etl.linkedpipes.com/ontology/version":
                    version = ((Literal) value).intValue();
                    break;
                case "http://www.w3.org/2004/02/skos/core#prefLabel":
                    labels.add(value);
                    break;
                case "http://etl.linkedpipes.com/ontology/tag":
                    tags.add(value);
                    break;
                default:
                    break;
            }
            return null;
        }

    }

    /**
     * Path to the file that contains the pipeline.
     */
    private final File file;

    /**
     * Reference to the pipeline info.
     */
    private Info info;

    /**
     * RDF reference to this pipeline. This can be used to provide a client
     * with reference to the pipeline where the complete
     * pipeline definition is not required.
     */
    private Collection<Statement> referenceRdf = new ArrayList<>(8);

    Pipeline(File file, Info info) {
        this.file = file;
        this.info = info;
    }

    public File getFile() {
        return file;
    }

    /**
     * @return Can be null if no information has been loaded.
     */
    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    /**
     * @return Null if pipeline information has not yet been loaded.
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
