package com.linkedpipes.etl.library.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Date;

/**
 * Interface for building statements from code.
 */
public class StatementsBuilder extends Statements {

    private Resource defaultGraph = null;

    public StatementsBuilder(Collection<Statement> collection) {
        super(collection);
    }

    /**
     * If no graph is provided when adding into this colleciotn, this
     * graph is used.
     */
    public void setDefaultGraph(Resource defaultGraph) {
        this.defaultGraph = defaultGraph;
    }

    public void addType(String s, String type) {
        addType(valueFactory.createIRI(s), type);
    }

    public void addType(Resource s, String type) {
        addIri(s, RDF.TYPE, type);
    }

    public void addIri(String s, String p, String o) {
        if (o == null) {
            return;
        }
        addIri(valueFactory.createIRI(s), p, o);
    }

    public void addIri(Resource s, String p, String o) {
        if (o == null) {
            return;
        }
        add(s, p, valueFactory.createIRI(o));
    }

    public void addIri(String s, IRI p, String o) {
        if (o == null) {
            return;
        }
        addIri(valueFactory.createIRI(s), p, o);
    }

    public void addIri(Resource s, IRI p, String o) {
        if (o == null) {
            return;
        }
        add(s, p, valueFactory.createIRI(o));
    }

    public void add(String s, String p, String o) {
        if (o == null) {
            return;
        }
        add(valueFactory.createIRI(s), p, o);
    }

    public void add(Resource s, String p, String o) {
        if (o == null) {
            return;
        }
        add(s, p, valueFactory.createLiteral(o));
    }

    public void add(String s, IRI p, String o) {
        if (o == null) {
            return;
        }
        add(valueFactory.createIRI(s), p, valueFactory.createLiteral(o));
    }

    public void add(Resource s, IRI p, String o) {
        if (o == null) {
            return;
        }
        add(s, p, valueFactory.createLiteral(o));
    }

    public void add(String s, String p, Integer o) {
        if (o == null) {
            return;
        }
        add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void add(Resource s, String p, Integer o) {
        if (o == null) {
            return;
        }
        add(s, p, valueFactory.createLiteral(o));
    }

    public void add(Resource s, IRI p, Integer o) {
        if (o == null) {
            return;
        }
        add(s, p, valueFactory.createLiteral(o));
    }

    public void add(String s, String p, Boolean o) {
        if (o == null) {
            return;
        }
        add(s, valueFactory.createIRI(p), o);
    }

    public void add(Resource s, String p, Boolean o) {
        if (o == null) {
            return;
        }
        add(s, valueFactory.createIRI(p), o);
    }

    public void add(String s, IRI p, Boolean o) {
        if (o == null) {
            return;
        }
        add(valueFactory.createIRI(s), p, o);
    }

    public void add(Resource s, IRI p, Boolean o) {
        if (o == null) {
            return;
        }
        add(s, p, valueFactory.createLiteral(o));
    }

    public void add(String s, String p, Date o) {
        if (o == null) {
            return;
        }
        add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void add(Resource s, String p, Date o) {
        if (o == null) {
            return;
        }
        add(s, valueFactory.createIRI(p), o);
    }

    public void add(Resource s, IRI p, Date o) {
        if (o == null) {
            return;
        }
        add(s, p, valueFactory.createLiteral(o));
    }

    public void add(String s, String p, Long o) {
        if (o == null) {
            return;
        }
        add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void add(Resource s, String p, Long o) {
        if (o == null) {
            return;
        }
        add(s, valueFactory.createIRI(p), valueFactory.createLiteral(o));
    }

    public void add(Resource s, String p, Value o) {
        if (o == null) {
            return;
        }
        add(s, valueFactory.createIRI(p), o);
    }


    public void add(String s, String p, TemporalAccessor o) {
        if (o == null) {
            return;
        }
        add(
                valueFactory.createIRI(s),
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void add(Resource s, String p, TemporalAccessor o) {
        if (o == null) {
            return;
        }
        add(
                s,
                valueFactory.createIRI(p),
                valueFactory.createLiteral(o));
    }

    public void add(Resource s, IRI p, Value o) {
        if (o == null) {
            return;
        }
        collection.add(valueFactory.createStatement(
                s, p, o, defaultGraph));
    }

    public void addList(Resource s, String p, Collection<Resource> o) {
        addList(s, valueFactory.createIRI(p), o);
    }

    public void addList(Resource s, IRI p, Collection<Resource> o) {
        Resource last = null;
        for (Resource resource : o) {
            Resource next = valueFactory.createBNode();
            if (last == null) {
                // This is the first element.
                add(s, p, next);
            } else {
                add(last, RDF.REST, next);
            }
            add(next, RDF.FIRST, resource);
            last = next;
        }
        if (last != null) {
            add(last, RDF.REST, RDF.NIL);
        }
    }

}
