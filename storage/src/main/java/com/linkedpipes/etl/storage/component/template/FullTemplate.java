package com.linkedpipes.etl.storage.component.template;

import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * A full template that points towards a pipeline or a JAR file.
 * Contains full definition together with the dialogs and resources.
 *
 * @author Petr Škoda
 */
class FullTemplate extends BaseTemplate {

    public static final IRI TYPE;

    static {
        TYPE = SimpleValueFactory.getInstance().createIRI(
                "http://linkedpipes.com/ontology/JarTemplate");
    }

    /**
     * Represent a dialog.
     */
    public static class Dialog {

        /**
         * Name of the dialog.
         */
        private String name;

        /**
         * The dialog directory.
         */
        private File root;

        public Dialog() {
        }

        public String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        public File getRoot() {
            return root;
        }

        void setRoot(File root) {
            this.root = root;
        }

    }

    private Map<String, Dialog> dialogs = Collections.EMPTY_MAP;

    public FullTemplate() {
    }

    void setIri(String iri) {
        this.iri = iri;
    }

    public Map<String, Dialog> getDialogs() {
        return Collections.unmodifiableMap(dialogs);
    }

    public void setDialogs(Map<String, Dialog> dialogs) {
        this.dialogs = dialogs;
    }

}
