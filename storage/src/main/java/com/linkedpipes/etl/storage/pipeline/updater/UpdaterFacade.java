package com.linkedpipes.etl.storage.pipeline.updater;

import com.linkedpipes.etl.storage.BaseException;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Perform update operations on the given pipeline. The pipeline must be
 * of current version.
 *
 * @author Petr Škoda
 */
@Service
public class UpdaterFacade {

    public static class UpdateFailed extends BaseException {

        public UpdateFailed(String message, Object... args) {
            super(message, args);
        }

    }

    public interface Options {

        /**
         * @return If not null name of the pipeline should be set to this value.
         */
        public Collection<Literal> getLabels();

    }

    public Collection<Statement> update(Collection<Statement> pipelineRdf,
            IRI pipelineIri, Options options)
            throws UpdateFailed {
        // Parse pipeline.
        final List<Statement> result = new ArrayList<>(pipelineRdf.size() + 16);
        final List<Statement> pplInstance = new LinkedList<>();
        for (Statement statement : pipelineRdf) {
            if (statement.getSubject().equals(pipelineIri)) {
                pplInstance.add(statement);
            } else {
                result.add(statement);
            }
        }
        // Update.
        if (options.getLabels() != null && !options.getLabels().isEmpty()) {
            updateLabels(pplInstance, options.getLabels());
        }
        //
        result.addAll(pplInstance);
        return result;
    }

    /**
     * Perform in-place update of pipeline label.
     *
     * @param pipelineInstance Statements describing the pipeline.
     * @param labels
     */
    private void updateLabels(List<Statement> pipelineInstance,
            Collection<Literal> labels) throws UpdateFailed {
        // Remove existing label statements.
        final List<Statement> toRemove = new ArrayList<>(2);
        for (Statement statement : pipelineInstance) {
            if (SKOS.PREF_LABEL.equals(statement.getPredicate())) {
                toRemove.add(statement);
            }
        }
        pipelineInstance.removeAll(toRemove);
        // Add labels.
        final Resource pipelineResource = pipelineInstance.get(0).getSubject();
        final Resource graph = pipelineInstance.get(0).getContext();
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        for (Value value : labels) {
            pipelineInstance.add(valueFactory.createStatement(pipelineResource,
                    SKOS.PREF_LABEL, value, graph));
        }
    }

}
