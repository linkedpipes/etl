package com.linkedpipes.etl.storage.component.pipeline;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

import java.io.File;
import java.util.*;

/**
 * @author Petr Škoda
 */
public class Pipeline {

    public static final IRI TYPE;

    public static final IRI VERSION;

    /**
     * Current pipeline version.
     */
    public static final int VERSION_NUMBER = 1;

    static {
        TYPE = SimpleValueFactory.getInstance().createIRI(
                "http://linkedpipes.com/ontology/Pipeline");
        VERSION = SimpleValueFactory.getInstance().createIRI(
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

        /**
         * List of used components and number of usage.
         */
        private final Map<String, Integer> componentUsage = new HashMap<>();

        Info() {
        }

        public String getIri() {
            return iri;
        }

        public List<Value> getLabels() {
            return Collections.unmodifiableList(labels);
        }

        public Map<String, Integer> getComponentUsage() {
            return componentUsage;
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
                case "http://linkedpipes.com/ontology/template":
                    final String template = value.stringValue();
                    componentUsage.putIfAbsent(template, 0);
                    componentUsage.put(template,
                            componentUsage.get(template) + 1);
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
    private Info info = null;

    /**
     * RDF reference to the pipeline used in the pipeline list.
     */
    private Collection<Statement> referenceRdf = new ArrayList<>(4);

    Pipeline(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    /**
     *
     * @return
     */
    public Info getInfo() {
        return info;
    }

    void setInfo(Info info) {
        this.info = info;
    }

    /**
     *
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

    /**
     * Create and return an empty representation of a pipeline.
     *
     * @param iriAsString
     * @return
     */
    public static Collection<Statement> createEmpty(String iriAsString) {
        final List<Statement> pipeline = new ArrayList<>(4);
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final IRI iri = vf.createIRI(iriAsString);
        //
        pipeline.add(vf.createStatement(iri, RDF.TYPE, Pipeline.TYPE, iri));
        pipeline.add(vf.createStatement(iri, SKOS.PREF_LABEL,
                vf.createLiteral(iriAsString), iri));

        pipeline.add(vf.createStatement(iri, VERSION,
                vf.createLiteral(VERSION_NUMBER), iri));

        return pipeline;
    }

}
