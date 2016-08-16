package com.linkedpipes.etl.storage.component.pipeline;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Designed to load a information from pipeline definition and
 * update the pipeline.
 *
 * @author Petr Škoda
 */
final class PipelineLoader {


    private PipelineLoader() {

    }

    public static Pipeline.Info getInfo(Collection<Statement> statements)
            throws PojoLoader.CantLoadException {
        final Pipeline.Info info = new Pipeline.Info();
        PojoLoader.loadOfType(statements, Pipeline.TYPE, info);
        return info;
    }

    /**
     * Update a pipeline reference based on the pipeline info.
     *
     * @param pipeline
     * @param info
     */
    public static void updadeReference(Pipeline pipeline, Pipeline.Info info) {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final IRI referenceIri = vf.createIRI(pipeline.getIri() + "/reference");
        final String pipelineIri = pipeline.getIri();
        final List<Statement> referenceRdf = new ArrayList<>(4);
        //
        referenceRdf.add(vf.createStatement(referenceIri,
                RDF.TYPE,
                vf.createIRI("http://etl.linkedpipes.com/ontology/Reference"),
                referenceIri));
        referenceRdf.add(vf.createStatement(referenceIri,
                vf.createIRI("http://linkedpipes.com/ontology/pipeline"),
                vf.createIRI(pipelineIri),
                referenceIri));
        final String pipelineId = pipelineIri.substring(
                pipelineIri.lastIndexOf("/") + 1);
        referenceRdf.add(vf.createStatement(referenceIri,
                vf.createIRI("http://linkedpipes.com/ontology/id"),
                vf.createLiteral(pipelineId),
                referenceIri));
        for (Value label : info.getLabels()) {
            referenceRdf.add(vf.createStatement(referenceIri, SKOS.PREF_LABEL,
                    label, referenceIri));
        }
        //
        pipeline.setReferenceRdf(referenceRdf);
    }

}
