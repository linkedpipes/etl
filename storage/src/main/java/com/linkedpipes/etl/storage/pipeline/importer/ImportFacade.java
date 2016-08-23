package com.linkedpipes.etl.storage.pipeline.importer;

import com.linkedpipes.etl.storage.pipeline.Pipeline;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Update resources in pipeline. Can be used to import pipelines.
 *
 * @author Petr Škoda
 */
@Service
public class ImportFacade {

    public interface Options {

        /**
         * Pipeline IRI. If set pipeline resources are updated.
         *
         * @return
         */
        public IRI getPipelineIri();

    }

    /**
     * Given a pipeline return imported version. The new version may share
     * statements with given pipeline.
     *
     * @return
     */
    public Collection<Statement> update(Collection<Statement> pipelineRdf,
            Options options) {
        if (options.getPipelineIri() != null) {
            pipelineRdf = updateResources(pipelineRdf,
                    options.getPipelineIri().stringValue());
        }

        return pipelineRdf;
    }

    /**
     * Change resource IRI for pipeline resources.
     *
     * @param pipelineRdf
     * @param baseIri
     * @return
     */
    private static Collection<Statement> updateResources(
            Collection<Statement> pipelineRdf, String baseIri) {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final Map<Resource, Resource> mapping = new HashMap<>();
        for (Statement s : pipelineRdf) {
            if (s.getPredicate().equals(RDF.TYPE) &&
                    !mapping.containsKey(s.getSubject())) {
                if (s.getObject().equals(Pipeline.TYPE)) {
                    // For pipeline we the IRI as it is.
                    mapping.put(s.getSubject(), valueFactory.createIRI(
                            baseIri));
                } else {
                    mapping.put(s.getSubject(), valueFactory.createIRI(
                            baseIri + "/" + (mapping.size() + 1)));
                }
            }
        }
        final List<Statement> result = new ArrayList<>(pipelineRdf.size());
        for (Statement s : pipelineRdf) {
            final Resource context = mapping.getOrDefault(
                    s.getContext(), s.getContext());
            if (mapping.containsKey(s.getSubject())) {
                if (mapping.containsKey(s.getObject())) {
                    result.add(valueFactory.createStatement(
                            mapping.get(s.getSubject()), s.getPredicate(),
                            mapping.get(s.getObject()), context));
                } else {
                    result.add(valueFactory.createStatement(
                            mapping.get(s.getSubject()), s.getPredicate(),
                            s.getObject(), context));
                }
            } else {
                if (mapping.containsKey(s.getObject())) {
                    result.add(valueFactory.createStatement(
                            s.getSubject(), s.getPredicate(),
                            mapping.get(s.getObject()), context));
                } else {
                    result.add(valueFactory.createStatement(
                            s.getSubject(), s.getPredicate(), s.getObject(),
                            context));
                }
            }
        }
        return result;
    }

}
