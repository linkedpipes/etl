package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Provide capabilities to unpack pipeline for execution.
 */
@Service
public class UnpackerFacade {

    @Autowired
    private PipelineFacade pipelines;

    @Autowired
    private TemplateFacade templates;

    @Autowired
    private Configuration configuration;

    public Collection<Statement> unpack(Collection<Statement> pipelineRdf,
            Collection<Statement> configurationRdf) throws BaseException {
        // Parse options.
        final UnpackOptions options = new UnpackOptions();
        PojoLoader.loadOfType(configurationRdf, UnpackOptions.TYPE, options);
        //
        String pipelineIri = null;
        for (Statement statement : pipelineRdf) {
            if (statement.getPredicate().equals(RDF.TYPE) &&
                    statement.getObject().stringValue().equals(
                            "http://linkedpipes.com/ontology/Pipeline")) {
                pipelineIri = statement.getSubject().stringValue();
                break;
            }
        }
        if (pipelineIri == null) {
            throw new BaseException("Missing pipeline resource.");
        }
        // Unpack.
        return Unpacker.update(pipelineRdf, templates,
                pipelineIri, options, configuration);
    }

    public Collection<Statement> unpack(Pipeline pipeline,
            Collection<Statement> configurationRdf) throws BaseException {
        // Parse options.
        final UnpackOptions options = new UnpackOptions();
        PojoLoader.loadOfType(configurationRdf, UnpackOptions.TYPE, options);
        // Unpack.
        return Unpacker.update(pipelines.getPipelineRdf(pipeline), templates,
                pipeline.getIri(), options, configuration);
    }

}
