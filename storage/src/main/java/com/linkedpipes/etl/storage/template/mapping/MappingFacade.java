package com.linkedpipes.etl.storage.template.mapping;

import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.Template;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;

/**
 * A single component can have multiple IRIs, those
 * IRIs may appear as the template is imported from one instance to
 * another.
 *
 * This facade should provide functionality for supporting tracking and
 * resolving of these IRIs. Each template can be either original - created
 * from JarTemplate of Template on some instance or it could be imported.
 *
 * For imported templates we keep track of the original template using
 * owl:sameAs predicate.
 *
 * The mapping facade use fixed graph to store RDF data. The triples are of
 * shape ORIGINAL_IRI predicate LOCAL_IRI.
 *
 * TODO Move mapping file into templates directory.
 */
@Service
public class MappingFacade {

    private static final Logger LOG
            = LoggerFactory.getLogger(MappingFacade.class);

    /**
     * Name of graph used to store mapping data.
     */
    private static final IRI GRAPH;

    private static final String MAPPING_FILE = "mapping.trig";

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        GRAPH = valueFactory.createIRI(
                "http://etl.linkedpipes.com/resources/plugins/mapping");
    }

    private Configuration configuration;

    @Autowired
    public MappingFacade(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Mapping original template -> local component.
     */
    private Map<String, String> originalToLocal = new HashMap<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @PostConstruct
    public void initialize() throws RdfUtils.RdfException {
        File mappingFile = getMappingFile();
        configuration.getKnowledgeDirectory().mkdirs();
        if (!mappingFile.exists()) {
            return;
        }
        loadMappingFromFile(mappingFile);
    }

    private File getMappingFile() {
        return new File(configuration.getKnowledgeDirectory(), MAPPING_FILE);
    }

    private void loadMappingFromFile(File mappingFile)
            throws RdfUtils.RdfException {
        Collection<Statement> statements = RdfUtils.read(mappingFile);

        statements.stream()
                .filter((s) -> s.getContext().equals(GRAPH))
                .filter((s) -> s.getPredicate().equals(OWL.SAMEAS))
                .forEach((s) -> addMapping(s));

    }

    private void addMapping(Statement statement) {
        originalToLocal.put(statement.getSubject().stringValue(),
                statement.getObject().stringValue());
    }

    public Collection<Statement> exportForTemplates(
            Collection<Template> templates) {
        List<Statement> output = new ArrayList<>(templates.size());
        for (Template template : templates) {
            String originalIri = getOriginalIriForLocalIri(template.getIri());
            if (originalIri == null) {
                continue;
            }
            output.add(valueFactory.createStatement(
                    valueFactory.createIRI(originalIri),
                    OWL.SAMEAS,
                    valueFactory.createIRI(template.getIri()),
                    GRAPH));
        }
        return output;
    }

    private String getOriginalIriForLocalIri(String iri) {
        for (Map.Entry<String, String> entry : originalToLocal.entrySet()) {
            if (entry.getValue().equals(iri)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Does NOT import mapping.
     */
    public Mapping createMappingFromStatements(
            Collection<Statement> statements) {
        MappingFromStatements factory = new MappingFromStatements(
                Collections.unmodifiableMap(this.originalToLocal), GRAPH);
        return factory.create(statements);
    }

    /**
     * Add mapping from given template (local) to original IRI.
     */
    public void add(Template template, String originalIri) {
        if (originalToLocal.containsKey(originalIri)) {
            LOG.error("There is already a local template form given IRI.");
        }
        originalToLocal.put(originalIri, template.getIri());
    }

    /**
     * Remove toLocal for given template.
     */
    public void remove(Template template) {
        originalToLocal.values().remove(template);
    }

    /**
     * Should be called after any call (or sequence of calls) of
     * {@link #add(Template, String)}.
     */
    public void save() {
        File mappingFile = getMappingFile();
        Collection<Statement> statements = this.collectAsStatements();
        try {
            RdfUtils.write(mappingFile, RDFFormat.TRIG, statements);
        } catch (RdfUtils.RdfException ex) {
            LOG.error("Can't save toLocal.", ex);
        }
    }

    private Collection<Statement> collectAsStatements() {
        List<Statement> output = new ArrayList<>(originalToLocal.size());
        for (Map.Entry<String, String> entry : originalToLocal.entrySet()) {
            output.add(valueFactory.createStatement(
                    valueFactory.createIRI(entry.getKey()),
                    OWL.SAMEAS,
                    valueFactory.createIRI(entry.getValue()),
                    GRAPH));
        }
        return output;
    }

}
