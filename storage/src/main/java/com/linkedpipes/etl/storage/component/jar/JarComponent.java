package com.linkedpipes.etl.storage.component.jar;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;

import java.io.File;

/**
 * @author Petr Škoda
 */
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

    public String getIri() {
        return iri;
    }

    @Override
    public void loadIri(String iri) {
        this.iri = iri;
    }

}
