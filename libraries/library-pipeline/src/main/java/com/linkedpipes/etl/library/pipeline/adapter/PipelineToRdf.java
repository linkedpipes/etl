package com.linkedpipes.etl.library.pipeline.adapter;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.library.pipeline.model.PipelineConnection;
import com.linkedpipes.etl.library.pipeline.model.PipelineDataFlow;
import com.linkedpipes.etl.library.pipeline.model.PipelineExecutionFlow;
import com.linkedpipes.etl.library.pipeline.model.PipelineExecutionProfile;
import com.linkedpipes.etl.library.pipeline.vocabulary.LP_V1;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class PipelineToRdf {

    public static Statements asRdf(Pipeline pipeline) {
        StatementsBuilder result = Statements.arrayList().builder();
        result.setDefaultGraph(pipeline.resource());
        //
        result.addType(pipeline.resource(), LP_V1.PIPELINE);
        result.add(pipeline.resource(),
                LP_V1.PREF_LABEL,
                pipeline.label());
        result.add(pipeline.resource(),
                LP_V1.HAS_VERSION,
                pipeline.version());
        result.add(pipeline.resource(),
                LP_V1.NOTE,
                pipeline.note());
        //
        result.add(pipeline.resource(),
                LP_V1.HAS_PROFILE,
                pipeline.executionProfile().resource());
        writeExecutionProfile(pipeline.executionProfile(), result);
        // With next version we can add a link from
        // pipeline to components and connections.
        for (PipelineComponent component : pipeline.components()) {
            writeComponent(component, result);
        }
        for (PipelineConnection connection : pipeline.connections()) {
            if (connection instanceof PipelineDataFlow flow) {
                writeDataFlow(flow, result);
            }
            if (connection instanceof PipelineExecutionFlow flow) {
                writeExecutionFlow(flow, result);
            }
        }
        return result;
    }

    protected static void writeExecutionProfile(
            PipelineExecutionProfile profile, StatementsBuilder statements) {
        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
        statements.addType(profile.resource(), LP_V1.PROFILE);
        statements.add(profile.resource(),
                LP_V1.HAS_RDF_REPOSITORY_POLICY,
                profile.rdfRepositoryPolicy());
        statements.add(profile.resource(),
                LP_V1.HAS_RDF_REPOSITORY_TYPE,
                profile.rdfRepositoryType());
        statements.add(profile.resource(),
                LP_V1.HAS_LOG_RETENTION,
                profile.logRetentionPolicy().asIri());
        statements.add(profile.resource(),
                LP_V1.HAS_LOG_RETENTION,
                profile.logRetentionPolicy().asIri());
        statements.add(profile.resource(),
                LP_V1.HAS_FAILED_EXECUTION_LIMIT,
                valueFactory.createLiteral(
                        profile.failedExecutionLimit()));
        statements.add(profile.resource(),
                LP_V1.HAS_SUCCESSFUL_EXECUTION_LIMIT,
                valueFactory.createLiteral(
                        profile.successfulExecutionLimit()));
    }

    protected static void writeComponent(
            PipelineComponent definition, StatementsBuilder statements) {
        statements.addType(definition.resource(), LP_V1.COMPONENT);
        statements.add(definition.resource(),
                LP_V1.PREF_LABEL,
                definition.label());
        statements.add(definition.resource(),
                LP_V1.HAS_DESCRIPTION,
                definition.description());
        statements.add(definition.resource(),
                LP_V1.NOTE,
                definition.note());
        statements.add(definition.resource(),
                LP_V1.HAS_COLOR,
                definition.color());
        statements.add(definition.resource(),
                LP_V1.HAS_CONFIGURATION_GRAPH,
                definition.configurationGraph());
        statements.add(definition.resource(),
                LP_V1.HAS_X,
                definition.xPosition());
        statements.add(definition.resource(),
                LP_V1.HAS_Y,
                definition.yPosition());
        statements.add(definition.resource(),
                LP_V1.HAS_TEMPLATE,
                definition.template());
        if (definition.disabled() != null &&
                !definition.disabled().booleanValue()) {
            statements.add(definition.resource(),
                    LP_V1.HAS_DISABLED,
                    SimpleValueFactory.getInstance().createLiteral(false));
        }
        if (!definition.configuration().isEmpty()) {
            Statements configuration =
                    Statements.wrap(definition.configuration());
            statements.addAll(
                    configuration.withGraph(definition.configurationGraph()));
        }
    }

    protected static void writeDataFlow(
            PipelineDataFlow definition, StatementsBuilder statements) {
        statements.addType(definition.resource(), LP_V1.CONNECTION);
        statements.add(definition.resource(),
                LP_V1.HAS_SOURCE_COMPONENT,
                definition.source());
        statements.add(definition.resource(),
                LP_V1.HAS_SOURCE_BINDING,
                definition.sourceBinding());
        statements.add(definition.resource(),
                LP_V1.HAS_TARGET_COMPONENT,
                definition.target());
        statements.add(definition.resource(),
                LP_V1.HAS_TARGET_BINDING,
                definition.targetBinding());
    }

    protected static void writeExecutionFlow(
            PipelineExecutionFlow definition, StatementsBuilder statements) {
        statements.addType(definition.resource(), LP_V1.RUN_AFTER);
        statements.add(definition.resource(),
                LP_V1.HAS_SOURCE_COMPONENT,
                definition.source());
        statements.add(definition.resource(),
                LP_V1.HAS_TARGET_COMPONENT,
                definition.target());
    }

}
