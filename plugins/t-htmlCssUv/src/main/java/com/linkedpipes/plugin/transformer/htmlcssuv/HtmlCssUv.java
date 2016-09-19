package com.linkedpipes.plugin.transformer.htmlcssuv;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * @author Petr Škoda
 */
public class HtmlCssUv implements Component.Sequential {

    public static final String WEB_PAGE_NAME = "webPage";

    public static final String SUBJECT_URI_TEMPLATE = "http://localhost/temp/";

    public static final String RDF_TYPE_PREDICATE
            = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private static final Logger LOG = LoggerFactory.getLogger(HtmlCssUv.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inFilesHtml;

    @Component.OutputPort(id = "OutputRdf")
    public WritableGraphListDataUnit outRdfData;

    @Component.Configuration
    public HtmlCssUvConfiguration config;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    /**
     * Used to generate original subjects.
     */
    private int subjectIndex = 0;

    private List<Statement> statements = new LinkedList<>();

    private IRI currentGraph;

    final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private void add(Resource s, IRI p, Value o) {
        statements.add(valueFactory.createStatement(s, p, o, currentGraph));
    }

    @Override
    public void execute() throws LpException {
        final IRI predicateSource = valueFactory.createIRI(
                HtmlCssUvOntology.PREDICATE_SOURCE);
        for (FilesDataUnit.Entry entry : inFilesHtml) {
            LOG.info("Parsing file: {}", entry);
            currentGraph = outRdfData.createGraph();
            final File entryFile = entry.toFile();
            final IRI rootSubject = valueFactory.createIRI(
                    entryFile.toURI().toString());
            // Read and parse document.
            try {
                final Document doc = Jsoup.parse(entryFile, null);
                parse(valueFactory, doc, rootSubject);
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't parse file: {}",
                        entry.getFileName(), ex);
            }
            // Add "metadata"
            if (config.getClassAsStr() != null
                    && !config.getClassAsStr().isEmpty()) {
                // Class for root object.
                final IRI rootClass =
                        valueFactory.createIRI(config.getClassAsStr());
                add(rootSubject, valueFactory.createIRI(
                        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        rootClass);
            }
            if (config.isSourceInformation()) {
                // Symbolic name of a source file.
                add(rootSubject, predicateSource,
                        valueFactory.createLiteral(entry.getFileName()));
            }
        }
    }

    private void parse(ValueFactory valueFactory, Document doc, IRI rootSubject)
            throws LpException {
        final IRI defaultHasPredicate =
                createIri(valueFactory, config.getHasPredicateAsStr());
        // Parse.
        final Stack<NamedData> states = new Stack();
        states.add(new NamedData(WEB_PAGE_NAME, doc.getAllElements(),
                rootSubject, null, null));
        final IRI rdfType = valueFactory.createIRI(RDF_TYPE_PREDICATE);
        while (!states.isEmpty()) {
            final NamedData state = states.pop();
            for (HtmlCssUvConfiguration.Action action : config.getActions()) {
                if (action.getType() == null) {
                    throw exceptionFactory.failure("Missing action type!");
                }
                // Execute action.
                switch (action.getType()) {
                    case ATTRIBUTE:
                        checkElementNotNull(state);
                        // Check for attribute existence.
                        // If it exists then extract its value.
                        if (state.elements.size() == 1
                                && state.elements.get(0)
                                .hasAttr(action.getActionData())) {
                            states.add(new NamedData(state, action,
                                    state.elements.get(0)
                                            .attr(action.getActionData())));
                        } else {
                            throw exceptionFactory.failure(
                                    "Element does not have required attribute:" +
                                            "{} action: {} html: {}",
                                    action.getActionData(), action.getName(),
                                    state.elements.html());
                        }
                        break;
                    case HTML:
                        checkElementNotNull(state);
                        // Get value as html.
                        states.add(new NamedData(state, action,
                                state.elements.html()));
                        break;
                    case OUTPUT:
                        // Output string value as RDF statement.
                        if (state.value == null) {
                            // Nothing to output.
                            if (state.elements != null) {
                                throw exceptionFactory.failure(
                                        "No string value but JSOUP elements set for: {}",
                                        action.getActionData());
                            }
                        }
                        // Create triple.
                        add(state.subject,
                                valueFactory.createIRI(action.getActionData()),
                                valueFactory.createLiteral(state.value));
                        // Create triple with type.
                        if (state.subjectClass != null) {
                            add(state.subject, rdfType, state.subjectClass);
                        }
                        // Connect to parent subject.
                        if (state.parentSubject != null
                                && state.hasPredicate != null) {
                            add(state.parentSubject,
                                    state.hasPredicate, state.subject);
                        }
                        break;
                    case QUERY:
                        checkElementNotNull(state);
                        // Execute query and store result.
                        states.add(new NamedData(state, action,
                                state.elements.select(
                                        action.getActionData())));
                        break;
                    case SUBJECT:
                        // Test given data.
                        final IRI hasPredicate = createIri(valueFactory,
                                action.getActionData());
                        final IRI newSubject = valueFactory.createIRI(
                                SUBJECT_URI_TEMPLATE + Integer.toString(
                                        subjectIndex++));
                        // Create a new subject with given type and put
                        // it into the tree.
                        states.add(new NamedData(state, action, newSubject,
                                null,
                                hasPredicate == null ? defaultHasPredicate :
                                        hasPredicate));
                        break;
                    case TEXT:
                        checkElementNotNull(state);
                        // Get value as a string.
                        states.add(new NamedData(state, action,
                                state.elements.text()));
                        break;
                    case UNLIST:
                        checkElementNotNull(state);
                        for (Element subElement : state.elements) {
                            states.add(new NamedData(state, action,
                                    new Elements(subElement)));
                        }
                        break;
                    case SUBJECT_CLASS:
                        final IRI classUri = createIri(valueFactory,
                                action.getActionData());
                        states.add(new NamedData(state, action, null, classUri,
                                null));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private IRI createIri(ValueFactory valueFactory, String uriString) {
        try {
            if (uriString != null && !uriString.isEmpty()) {
                return valueFactory.createIRI(uriString);
            }
        } catch (RuntimeException ex) {
            LOG.error("Can't generate URI for value: {}", uriString, ex);
        }
        return null;
    }

    private void checkElementNotNull(NamedData state) throws LpException {
        if (state.elements == null) {
            throw exceptionFactory.failure("Elements are null for action: {}",
                    state.name);
        }
    }

}
