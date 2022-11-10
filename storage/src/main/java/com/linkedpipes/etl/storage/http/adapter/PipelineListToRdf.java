package com.linkedpipes.etl.storage.http.adapter;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.assistant.AssistantService;
import com.linkedpipes.etl.storage.assistant.model.PipelineInfo;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import org.eclipse.rdf4j.model.Resource;

import java.util.Set;

public class PipelineListToRdf {

    private static final String PIPELINE =
            "http://linkedpipes.com/ontology/Pipeline";

    private static final String HAS_LABEL =
            "http://www.w3.org/2004/02/skos/core#prefLabel";

    private static final String HAS_TAG =
            "http://etl.linkedpipes.com/ontology/tag";

    /**
     * Implementation using pipeline facade.
     * This take 30ms, compare to 2ms using memory (assistant).
     * We can probably remove this method.
     * <p>
     * This method can be removed at any time in the future.
     */
    public static Statements asRdf(PipelineFacade pipelineFacade)
            throws StorageException {
        StatementsBuilder result = Statements.arrayList().builder();
        Set<Resource> pipelineResources = pipelineFacade.getPipelines();
        for (Resource resource : pipelineResources) {
            Pipeline pipeline = pipelineFacade.getPipeline(resource);
            result.setDefaultGraph(resource);
            result.addType(resource, PIPELINE);
            result.add(resource, HAS_LABEL, pipeline.label());
            for (String tag : pipeline.tags()) {
                result.add(resource, HAS_TAG, tag);
            }
        }
        return result;
    }

    public static Statements asRdf(AssistantService assistantService) {
        StatementsBuilder result = Statements.arrayList().builder();
        for (PipelineInfo pipeline : assistantService.getPipelineInfo()) {
            result.setDefaultGraph(pipeline.resource);
            result.addType(pipeline.resource, PIPELINE);
            result.add(pipeline.resource, HAS_LABEL, pipeline.label);
            for (String tag : pipeline.tags) {
                result.add(pipeline.resource, HAS_TAG, tag);
            }
        }
        return result;
    }

}
