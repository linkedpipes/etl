package com.linkedpipes.etl.library.pipeline.adapter;

import com.linkedpipes.etl.library.pipeline.vocabulary.LP_V1;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.pipeline.model.DataRetentionPolicy;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RdfToRawPipeline {

    private static final Logger LOG =
            LoggerFactory.getLogger(RdfToRawPipeline.class);

    private static final String PIPELINE = LP_V1.PIPELINE;

    public static List<RawPipeline> asRawPipelines(Statements statements) {
        StatementsSelector selector = statements.selector();
        return selector.selectByType(PIPELINE)
                .stream().map(statement -> safelyLoadPipeline(
                        selector,
                        statement.getSubject(),
                        statement.getContext()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static RawPipeline safelyLoadPipeline(
            StatementsSelector statements,
            Resource pipelineResource, Resource pipelineGraph) {
        try {
            return loadPipeline(statements, pipelineResource, pipelineGraph);
        } catch (RuntimeException ex) {
            LOG.error("Can't load pipeline '{}'.", pipelineGraph, ex);
            return null;
        }
    }

    private static RawPipeline loadPipeline(
            StatementsSelector statements,
            Resource pipelineResource, Resource pipelineGraph) {
        RawPipeline result = new RawPipeline();
        result.resource = pipelineResource;
        for (Statement statement : statements.withSubject(pipelineResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.PREF_LABEL:
                    if (value instanceof Literal literal) {
                        result.label = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_VERSION:
                    if (value instanceof Literal literal) {
                        result.version = literal.intValue();
                    }
                    break;
                case LP_V1.NOTE:
                    if (value instanceof Literal literal) {
                        result.note = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_TAG:
                    if (value instanceof Literal literal) {
                        result.tags.add(literal.stringValue());
                    }
                    break;
                case LP_V1.HAS_PROFILE:
                    if (value instanceof Resource resource) {
                        result.executionProfile.resource = resource;
                        loadExecutionProfile(
                                statements, result.executionProfile);
                    }
                    break;
                case LP_V1.HAS_CREATED:
                    if (value instanceof Literal literal) {
                        result.created = LocalDateTime.from(
                                literal.temporalAccessorValue());
                    }
                    break;
                case LP_V1.HAS_LAST_UPDATE:
                    if (value instanceof Literal literal) {
                        result.lastUpdate = LocalDateTime.from(
                                literal.temporalAccessorValue());
                    }
                    break;
                default:
                    break;
            }
        }
        // There is no connection from pipeline to components and
        // connections, so we load that by type.
        StatementsSelector definition =
                statements.selectByGraph(pipelineGraph).selector();
        for (Resource subject : definition.selectByType(
                LP_V1.COMPONENT).subjects()) {
            result.components.add(loadComponent(statements, subject));
        }
        result.components.sort(Comparator.comparing(
                item -> item.resource.stringValue()));
        for (Resource subject : definition.selectByType(
                LP_V1.RUN_AFTER).subjects()) {
            result.controlFlows.add(loadExecutionFlow(statements, subject));
        }
        result.controlFlows.sort(Comparator.comparing(
                item -> item.resource.stringValue()));
        for (Resource subject : definition.selectByType(
                LP_V1.CONNECTION).subjects()) {
            result.dataFlows.add(loadDataFlow(statements, subject));
        }
        result.dataFlows.sort(Comparator.comparing(
                item -> item.resource.stringValue()));
        sanitizeProfile(result);
        return result;
    }

    private static void loadExecutionProfile(
            StatementsSelector statements,
            RawPipelineExecutionProfile profile) {
        for (Statement statement : statements.withSubject(profile.resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.HAS_RDF_REPOSITORY_POLICY:
                    if (value instanceof IRI iri) {
                        profile.rdfRepositoryPolicy = iri;
                    }
                    break;
                case LP_V1.HAS_RDF_REPOSITORY_TYPE:
                    if (value instanceof IRI iri) {
                        profile.rdfRepositoryType = iri;
                    }
                    break;
                case LP_V1.HAS_LOG_RETENTION:
                    if (value instanceof IRI iri) {
                        profile.logRetentionPolicy = DataRetentionPolicy
                                .fromIri(iri.stringValue());
                    }
                    break;
                case LP_V1.HAS_DATA_RETENTION:
                    if (value instanceof IRI iri) {
                        profile.debugDataRetentionPolicy = DataRetentionPolicy
                                .fromIri(iri.stringValue());
                    }
                    break;
                case LP_V1.HAS_FAILED_EXECUTION_LIMIT:
                    if (value instanceof Literal literal) {
                        profile.failedExecutionLimit = literal.intValue();
                    }
                    break;
                case LP_V1.HAS_SUCCESSFUL_EXECUTION_LIMIT:
                    if (value instanceof Literal literal) {
                        profile.successfulExecutionLimit = literal.intValue();
                    }
                    break;
                default:
                    break;
            }
        }
    }


    private static RawPipelineComponent loadComponent(
            StatementsSelector statements, Resource componentResource) {
        RawPipelineComponent result = new RawPipelineComponent();
        result.resource = componentResource;
        for (Statement statement : statements.withSubject(componentResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.PREF_LABEL:
                    if (value instanceof Literal literal) {
                        result.label = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_DESCRIPTION:
                    if (value instanceof Literal literal) {
                        result.description = literal.stringValue();
                    }
                    break;
                case LP_V1.NOTE:
                    if (value instanceof Literal literal) {
                        result.note = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_COLOR:
                    if (value instanceof Literal literal) {
                        result.color = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_CONFIGURATION_GRAPH:
                    if (value instanceof Resource resource) {
                        result.configurationGraph = resource;
                    }
                    break;
                case LP_V1.HAS_X:
                    if (value instanceof Literal literal) {
                        result.x = literal.intValue();
                    }
                    break;
                case LP_V1.HAS_Y:
                    if (value instanceof Literal literal) {
                        result.y = literal.intValue();
                    }
                case LP_V1.HAS_TEMPLATE:
                    if (value instanceof Resource resource) {
                        result.template = resource;
                    }
                    break;
                case LP_V1.HAS_DISABLED:
                    if (value instanceof Literal literal) {
                        result.disabled = literal.booleanValue();
                    }
                default:
                    break;
            }
        }
        result.configuration = statements.selectByGraph(
                result.configurationGraph).withoutGraph();
        return result;
    }

    private static RawPipelineDataFlow loadDataFlow(
            StatementsSelector statements, Resource flowResource) {
        RawPipelineDataFlow result = new RawPipelineDataFlow();
        result.resource = flowResource;
        for (Statement statement : statements.withSubject(flowResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.HAS_SOURCE_BINDING:
                    result.sourceBinding = value;
                    break;
                case LP_V1.HAS_TARGET_BINDING:
                    result.targetBinding = value;
                    break;
                case LP_V1.HAS_SOURCE_COMPONENT:
                    if (value instanceof Resource resource) {
                        result.source = resource;
                    }
                    break;
                case LP_V1.HAS_TARGET_COMPONENT:
                    if (value instanceof Resource resource) {
                        result.target = resource;
                    }
                    break;
                default:
                    break;
            }
        }
        result.vertices.addAll(loadVertices(statements, flowResource));
        return result;
    }

    private static List<RawPipelineVertex> loadVertices(
            StatementsSelector statements, Resource connection) {
        return statements
                .select(connection, LP_V1.HAS_VERTEX, null)
                .objects()
                .stream().filter(Value::isResource)
                .map(item -> (Resource) item)
                .map(item -> loadVertex(statements, item))
                .toList();

    }

    private static RawPipelineVertex loadVertex(
            StatementsSelector statements, Resource connection) {
        RawPipelineVertex result = new RawPipelineVertex();
        result.resource = connection;
        for (Statement statement : statements.withSubject(connection)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.HAS_ORDER:
                    if (value instanceof Literal literal) {
                        result.order = literal.intValue();
                    }
                    break;
                case LP_V1.HAS_X:
                    if (value instanceof Literal literal) {
                        result.x = literal.intValue();
                    }
                    break;
                case LP_V1.HAS_Y:
                    if (value instanceof Literal literal) {
                        result.y = literal.intValue();
                    }
                    break;
            }
        }
        return result;
    }

    private static RawPipelineControlFlow loadExecutionFlow(
            StatementsSelector statements, Resource flowResource) {
        RawPipelineControlFlow result = new RawPipelineControlFlow();
        result.resource = flowResource;
        for (Statement statement : statements.withSubject(flowResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.HAS_SOURCE_COMPONENT:
                    if (value instanceof Resource resource) {
                        result.source = resource;
                    }
                    break;
                case LP_V1.HAS_TARGET_COMPONENT:
                    if (value instanceof Resource resource) {
                        result.target = resource;
                    }
                    break;
                default:
                    break;
            }
        }
        result.vertices.addAll(loadVertices(statements, flowResource));
        return result;
    }

    /**
     * Profile resource is determined by pipeline resource.
     */
    private static void sanitizeProfile(RawPipeline pipeline) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Resource resource;
        if (pipeline.resource.isBNode()) {
            resource = valueFactory.createBNode();
        } else {
            resource = valueFactory.createIRI(
                    pipeline.resource.stringValue() + "/profile/default");
        }
        pipeline.executionProfile.resource = resource;
    }

}
