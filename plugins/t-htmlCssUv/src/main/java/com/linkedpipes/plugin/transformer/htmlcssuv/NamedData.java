package com.linkedpipes.plugin.transformer.htmlcssuv;

import org.eclipse.rdf4j.model.IRI;
import org.jsoup.select.Elements;

/**
 * Store processing context.
 */
class NamedData {

    public final String name;

    /**
     * Value in elements if presented.
     */
    public final Elements elements;

    /**
     * Current subject.
     */
    public final IRI subject;

    /**
     * Subject class, used only if not null.
     */
    public final IRI subjectClass;

    /**
     * String value if presented.
     */
    public final String value;

    /**
     * Parent subject, we can connect to it.
     */
    public final IRI parentSubject;

    /**
     * Connection predicate between subject and parentSubject.
     */
    public final IRI hasPredicate;

    /**
     * Used to create a first object.
     *
     * @param name
     * @param elements
     * @param subject
     */
    public NamedData(String name, Elements elements, IRI subject,
            IRI parentSubject, IRI hasPredicate) {
        this.name = name;
        this.elements = elements;
        this.subject = subject;
        this.subjectClass = null;
        this.value = null;
        this.parentSubject = parentSubject;
        this.hasPredicate = hasPredicate;
    }

    /**
     * Create a copy of given subject, just set given elements.
     *
     * @param source
     * @param action
     * @param elements
     */
    public NamedData(NamedData source,
            HtmlCssUvConfiguration.Action action, Elements elements) {
        this.name = action.getOutputName();
        this.elements = elements;
        this.subject = source.subject;
        this.subjectClass = source.subjectClass;
        this.value = null;
        this.parentSubject = source.parentSubject;
        this.hasPredicate = source.hasPredicate;
    }

    /**
     * @param source
     * @param action
     * @param subject If null value form source is used!
     * @param subjectClass If null then parent value is used.
     * @param hasPredicate If null, then subject stay on same level.
     */
    public NamedData(NamedData source,
            HtmlCssUvConfiguration.Action action, IRI subject,
            IRI subjectClass, IRI hasPredicate) {
        this.name = action.getOutputName();
        this.elements = source.elements;
        this.subject = subject == null ? source.subject : subject;
        this.subjectClass =
                subjectClass == null ? source.subjectClass : subjectClass;
        this.value = source.value;
        if (hasPredicate == null) {
            // Same level.
            this.parentSubject = source.parentSubject;
            this.hasPredicate = source.hasPredicate;
        } else {
            // New level.
            this.parentSubject = source.subject;
            this.hasPredicate = hasPredicate;
        }
    }

    /**
     * Create a copy of given subject, just set given text.
     *
     * @param source
     * @param action
     * @param value
     */
    public NamedData(NamedData source, HtmlCssUvConfiguration.Action action,
            String value) {
        this.name = action.getOutputName();
        this.elements = null;
        this.subject = source.subject;
        this.subjectClass = source.subjectClass;
        this.value = value;
        this.parentSubject = source.parentSubject;
        this.hasPredicate = source.hasPredicate;
    }

}
