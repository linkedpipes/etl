package com.linkedpipes.etl.storage.jar;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.io.File;

public class JarComponent implements PojoLoader.Loadable {

    public static final IRI TYPE;

    static {
        TYPE = SimpleValueFactory.getInstance().createIRI(
                "http://etl.linkedpipes.com/ontology/JarFile");
    }

    /**
     * Path to the JAR file.
     */
    private final File file;

    /**
     * IRI identification of this component.
     */
    private String iri;

    JarComponent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return file.getName().replace(".jar", "");
    }

    public String getIri() {
        return iri;
    }

    @Override
    public void loadIri(String iri) {
        this.iri = iri;
    }

}
